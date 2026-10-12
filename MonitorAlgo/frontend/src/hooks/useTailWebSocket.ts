// useTailWebSocket.ts
import { useEffect, useRef } from "react";

type Options = {
    // Regex du timestamp en début de ligne (ex: "2025-08-09 22:37:21.937 | ...")
    tsRegex?: RegExp;
    // Si true, on supprime les lignes dont le timestamp < dernier affiché
    dedupeByTimestamp?: boolean;
};

export function useTailWebSocket(options: Options = {}) {
    const {
        dedupeByTimestamp = true,
        tsRegex = /^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3})\s\|/,
    } = options;

    const logRef = useRef<HTMLPreElement | null>(null);
    const wsRef = useRef<WebSocket | null>(null);
    const sessionIdRef = useRef<number>(0); // identifie la session courante
    const lineBufferRef = useRef<string>(""); // buffer pour morceaux de lignes

    // état pour dédup
    const lastTsRef = useRef<number | null>(null); // dernier timestamp accepté (ms)
    const lastLineRef = useRef<string>(""); // dernière ligne affichée

    const parseTs = (line: string): number | null => {
        const m = line.match(tsRegex);
        if (!m) return null;
        // m[1] = "YYYY-MM-DD HH:mm:ss.SSS"
        const [date, time] = m[1].split(" ");
        if (!date || !time) return null;
        // parse manuel pour éviter les surprises de timezone
        const [y, mo, d] = date.split("-").map(Number);
        const [hh, mm, ssMs] = time.split(":");
        const [ss, msStr] = ssMs.split(".");
        const year = y;
        const month = Number(mo) - 1;
        const day = Number(d);
        const hour = Number(hh);
        const minute = Number(mm);
        const sec = Number(ss);
        const ms = Number(msStr);
        // On calcule en UTC pour rester cohérent; seule l’ordre relatif nous intéresse.
        return Date.UTC(year, month, day, hour, minute, sec, ms);
    };

    const resetViewState = () => {
        lineBufferRef.current = "";
        lastTsRef.current = null;
        lastLineRef.current = "";
    };

    const appendLine = (line: string) => {
        const el = logRef.current;
        if (!el) return;

        // dédup par timestamp/contenu
        if (dedupeByTimestamp) {
            const ts = parseTs(line);
            const lastTs = lastTsRef.current;

            // 1) si timestamp présent et plus petit → on drop
            if (ts !== null && lastTs !== null && ts < lastTs) return;

            // 2) si même ts ET même contenu exact → on drop
            if (
                ts !== null &&
                lastTs !== null &&
                ts === lastTs &&
                line === lastLineRef.current
            )
                return;

            // ok: on accepte et on met à jour les refs
            if (ts !== null) lastTsRef.current = ts;
        } else {
            // même sans ts, on évite les exact dup consécutifs
            if (line === lastLineRef.current) return;
        }

        const wasAtBottom =
            el.scrollHeight - el.scrollTop - el.clientHeight < 5;
        if (el.innerText === "Chargement…") el.innerText = "";
        // plus rapide que innerText
        el.textContent += line + "\n";
        lastLineRef.current = line;
        if (wasAtBottom) el.scrollTop = el.scrollHeight;
    };

    const open = (url: string) => {
        // si déjà la même URL ouverte et socket OK → rien
        if (
            wsRef.current &&
            (wsRef.current as any)._url === url &&
            (wsRef.current.readyState === WebSocket.OPEN ||
                wsRef.current.readyState === WebSocket.CONNECTING)
        ) {
            return;
        }

        // nouvelle session
        sessionIdRef.current += 1;
        const sid = sessionIdRef.current;

        // ferme l’ancienne
        try {
            wsRef.current?.close();
        } catch {}
        wsRef.current = null;

        // reset affichage
        resetViewState();
        if (logRef.current) logRef.current.innerText = "Chargement…";

        const ws = new WebSocket(url);
        (ws as any)._url = url;

        ws.onopen = () => {
            // rien
        };

        ws.onmessage = (e) => {
            // ignore anciens sockets (StrictMode/délai)
            if (sid !== sessionIdRef.current) return;

            // Accumule dans un buffer et traite ligne par ligne
            lineBufferRef.current += String(e.data);
            const parts = lineBufferRef.current.split("\n");

            // toutes les lignes complètes sauf la dernière (peut être incomplète)
            for (let i = 0; i < parts.length - 1; i++) {
                const line = parts[i].replace(/\r$/, "");
                if (line) appendLine(line);
            }
            // garde la fin incomplète en buffer
            lineBufferRef.current = parts[parts.length - 1];
        };

        ws.onerror = () => {
            if (sid !== sessionIdRef.current) return;
            if (logRef.current && logRef.current.innerText === "Chargement…") {
                logRef.current.innerText = "Erreur de lecture du fichier.";
            }
        };

        ws.onclose = () => {
            if (sid !== sessionIdRef.current) return;
            // si le buffer contient une fin de ligne non vidée, on la pousse
            const rest = lineBufferRef.current.trim();
            if (rest) appendLine(rest);
            lineBufferRef.current = "";
        };

        wsRef.current = ws;
    };

    const close = () => {
        sessionIdRef.current += 1; // invalide tous les anciens callbacks
        try {
            wsRef.current?.close();
        } catch {}
        wsRef.current = null;
    };

    // cleanup global (unmount)
    useEffect(() => {
        return () => {
            close();
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return { logRef, open, close };
}
