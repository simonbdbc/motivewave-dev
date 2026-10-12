import React, { useEffect, useRef, useState } from "react";
import toast from "react-hot-toast";
import CardListDirectory from "components/Cards/CardListDirectory";
import CardLog from "components/Cards/CardLog";
import CardActionButton from "components/Cards/CardActionButton";

const apiCall = {
    label: "Run Update",
    path: "/api/algo/update-stats",
};

interface FileNode {
    name: string;
    path: string;
    isDirectory: boolean;
    children?: FileNode[];
}

const AlgoUpdateStatsPage: React.FC = () => {
    const [tree, setTree] = useState<FileNode[]>([]);
    const [openDirs, setOpenDirs] = useState<Record<string, boolean>>({});
    const [selected, setSelected] = useState<string | null>(null);
    const logRef = useRef<HTMLPreElement | null>(null);
    const wsRef = useRef<WebSocket | null>(null);

    useEffect(() => {
        fetch("/api/tree?source=algo-update-stats")
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
            }/ws-logfile?source=algo-update-stats&path=${encodeURIComponent(
                selected
            )}`
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

    // Trouve le chemin du 1er fichier du 1er dossier, ou null si rien
    const findFirstFileInTree = (nodes: FileNode[]): string | null => {
        for (const node of nodes) {
            if (node.isDirectory && node.children && node.children.length > 0) {
                const firstChild = node.children.find((n) => !n.isDirectory);
                if (firstChild) return firstChild.path;
            }
        }
        return null;
    };

    const refreshTreeAndSelectFirst = async () => {
        try {
            const res = await fetch("/api/tree?source=algo-update-stats");
            const nextTree: FileNode[] = await res.json();
            setTree(nextTree);
            const firstFile = findFirstFileInTree(nextTree);
            if (firstFile) {
                setSelected(firstFile);
            } else {
                setSelected(null); // facultatif, pour désélectionner tout
                toast.error("Aucun fichier trouvé dans le tree update stats.");
            }
        } catch (e: any) {
            toast.error(
                "Erreur lors du rafraîchissement du tree: " + (e.message || e)
            );
        }
    };

    return (
        <>
            <div className="flex flex-wrap">
                <div className="w-full lg:w-4/12 px-4 pb-6">
                    <div className="w-full lg:w-12/12 px-4 pb-6">
                        <CardActionButton
                            apiCall={apiCall}
                            listName="Algo Update Stats"
                            onAfterRun={refreshTreeAndSelectFirst}
                        />
                    </div>
                    <div className="w-full lg:w-12/12 px-4 pb-6">
                        <CardListDirectory
                            nodes={tree}
                            selected={selected}
                            onSelect={setSelected}
                            openDirs={openDirs}
                            onToggle={handleToggle}
                            listName="History Update Stats Files"
                        />
                    </div>
                </div>
                <div className="w-full lg:w-8/12 px-4 pb-6 max-h-[80vh] h-[80vh]">
                    <CardLog logRef={logRef} />
                </div>
            </div>
        </>
    );
};

export default AlgoUpdateStatsPage;
