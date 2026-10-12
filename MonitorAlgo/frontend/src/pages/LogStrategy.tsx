import CardLog from "components/Cards/CardLog";
import CardListDirectory from "components/Cards/CardListDirectory";
import React, { useEffect, useRef, useState } from "react";

interface FileNode {
    name: string;
    path: string;
    isDirectory: boolean;
    children?: FileNode[];
}

const LogStrategy: React.FC = () => {
    const [tree, setTree] = useState<FileNode[]>([]);
    const [openDirs, setOpenDirs] = useState<Record<string, boolean>>({});
    const [selected, setSelected] = useState<string | null>(null);
    const logRef = useRef<HTMLPreElement | null>(null);
    const wsRef = useRef<WebSocket | null>(null);

    useEffect(() => {
        fetch("/api/tree?source=strategy")
            .then((r) => r.json())
            .then(setTree);
        return () => wsRef.current?.close();
    }, []);

    useEffect(() => {
        if (!selected) {
            wsRef.current?.close();
            if (logRef.current)
                logRef.current.innerText = "Sélectionnez un fichier…";
            return;
        }
        wsRef.current?.close();
        if (logRef.current) logRef.current.innerText = "Chargement…";
        const wsProtocol =
            window.location.protocol === "https:" ? "wss:" : "ws:";
        // Fix: passer &path= au lieu de ?path= (car il y a déjà un ?)
        wsRef.current = new WebSocket(
            `${wsProtocol}//${
                window.location.host
            }/ws-logfile?source=strategy&path=${encodeURIComponent(selected)}`
        );
        wsRef.current.onmessage = (e) => {
            const el = logRef.current;
            if (!el) return;
            if (el.innerText === "Chargement…") el.innerText = "";
            const isAtBottom =
                el.scrollHeight - el.scrollTop - el.clientHeight < 5;
            el.innerText += e.data;
            if (isAtBottom) el.scrollTop = el.scrollHeight;
        };
        return () => wsRef.current?.close();
    }, [selected]);

    // Fonction pour plier/déplier les dossiers
    const handleToggle = (path: string) =>
        setOpenDirs((prev) => ({ ...prev, [path]: !prev[path] }));

    return (
        <div className="flex flex-wrap">
            <div className="w-full lg:w-4/12 px-4 pb-6 max-h-[80vh] h-[80vh]">
                <CardListDirectory
                    nodes={tree}
                    selected={selected}
                    onSelect={setSelected}
                    openDirs={openDirs}
                    onToggle={handleToggle}
                    listName="Strategy Files"
                />
            </div>
            <div className="w-full lg:w-8/12 px-4 pb-6 max-h-[80vh] h-[80vh]">
                <CardLog logRef={logRef} />
            </div>
        </div>
    );
};

export default LogStrategy;
