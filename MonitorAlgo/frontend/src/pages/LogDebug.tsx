import React, { useEffect, useRef, useState } from "react";
import CardListFile from "components/Cards/CardListFile";
import { type FileNode } from "interfaces/types";
import CardLog from "components/Cards/CardLog";

const LogDebug: React.FC = () => {
    const [files, setFiles] = useState<FileNode[]>([]);
    const [selected, setSelected] = useState<string | null>(null);
    const logRef = useRef<HTMLPreElement | null>(null);
    const wsRef = useRef<WebSocket | null>(null);

    useEffect(() => {
        fetch("/api/tree?source=motivewave")
            .then((r) => r.json())
            .then(setFiles);
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
        wsRef.current = new WebSocket(
            `${wsProtocol}//${
                window.location.host
            }/ws-logfile?source=motivewave&path=${encodeURIComponent(selected)}`
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

    return (
        <>
            <div className="flex flex-wrap">
                <div className="w-full lg:w-4/12 px-4 pb-6 max-h-[80vh] h-[80vh]">
                    <CardListFile
                        nodes={files}
                        selected={selected}
                        onSelect={setSelected}
                        listName="Debug Files"
                    />
                </div>
                <div className="w-full lg:w-8/12 px-4 pb-6 max-h-[80vh] h-[80vh]">
                    <CardLog logRef={logRef} />
                </div>
            </div>
        </>
    );
};

export default LogDebug;
