import React, { useEffect, useState } from "react";
import toast from "react-hot-toast";
import CardActionDateRangePicker from "components/Cards/CardActionDateRangePicker";
import CardListDirectory from "components/Cards/CardListDirectory";
import CardLog from "components/Cards/CardLog";
import { useTailWebSocket } from "hooks/useTailWebSocket";

const apiCall = {
    label: "Run Algo",
    path: "/api/algo/run",
};

interface FileNode {
    name: string;
    path: string;
    isDirectory: boolean;
    children?: FileNode[];
}

const AlgoBacktestPage: React.FC = () => {
    const [tree, setTree] = useState<FileNode[]>([]);
    const [openDirs, setOpenDirs] = useState<Record<string, boolean>>({});
    const [selected, setSelected] = useState<string | null>(null);

    // 1 seul WS + gestion du <pre>
    const {
        logRef,
        open: openWs,
        close: closeWs,
    } = useTailWebSocket({
        dedupeByTimestamp: true,
        // adapte le regex si ton format change
        tsRegex: /^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3})\s\|/,
    });

    // Charger l’arbo au montage
    useEffect(() => {
        fetch("/api/tree?source=algo-backtest")
            .then((r) => r.json())
            .then(setTree);
        return () => closeWs();
    }, []);

    useEffect(() => {
        if (!selected) {
            closeWs();
            if (logRef.current)
                logRef.current.innerText = "Sélectionnez un fichier…";
            return;
        }
        const wsProtocol =
            window.location.protocol === "https:" ? "wss:" : "ws:";
        const url = `${wsProtocol}//${
            window.location.host
        }/ws-logfile?source=algo-backtest&path=${encodeURIComponent(selected)}`;
        openWs(url);
    }, [selected, openWs, closeWs, logRef]);

    // Toggle dossiers
    const handleToggle = (path: string) =>
        setOpenDirs((prev) => ({ ...prev, [path]: !prev[path] }));

    // 1er fichier du 1er dossier
    const findFirstFileInTree = (nodes: FileNode[]): string | null => {
        for (const node of nodes) {
            if (node.isDirectory && node.children && node.children.length > 0) {
                const firstChild = node.children.find((n) => !n.isDirectory);
                if (firstChild) return firstChild.path;
            }
        }
        return null;
    };

    // Après run: refresh + auto-select premier fichier
    const refreshTreeAndSelectFirst = async () => {
        try {
            const res = await fetch("/api/tree?source=algo-backtest");
            const nextTree: FileNode[] = await res.json();
            setTree(nextTree);
            const firstFile = findFirstFileInTree(nextTree);
            if (firstFile) {
                setSelected(firstFile); // si identique, le hook ne ré-ouvrira pas inutilement
            } else {
                setSelected(null);
                toast.error("Aucun fichier trouvé dans le tree algo.");
            }
        } catch (e: any) {
            toast.error(
                "Erreur lors du rafraîchissement du tree: " + (e?.message || e)
            );
        }
    };

    return (
        <>
            <div className="flex flex-wrap">
                <div className="w-full lg:w-4/12 px-4 pb-6">
                    <div className="w-full lg:w-12/12 px-4 pb-6">
                        <CardActionDateRangePicker
                            apiCall={apiCall}
                            listName="Algo Range Date"
                            checkboxLabel="Ignore Range Date"
                            checkboxChecked={true}
                            onAfterRun={refreshTreeAndSelectFirst}
                        />
                    </div>
                    <div className="w-full lg:w-12/12 px-4 pb-6">
                        <CardListDirectory
                            nodes={tree}
                            selected={selected}
                            onSelect={(path) => {
                                // Comportement demandé : cliquer sur le même fichier → ne rien faire
                                if (path === selected) return;
                                setSelected(path);
                            }}
                            openDirs={openDirs}
                            onToggle={handleToggle}
                            listName="History Algo Backtest Files"
                        />
                    </div>
                </div>

                <div className="w-full lg:w-8/12 px-4 pb-6 max-h-[80vh] h-[80vh]">
                    {/* CardLog doit rendre un <pre ref={logRef}/> */}
                    <CardLog logRef={logRef} />
                </div>
            </div>
        </>
    );
};

export default AlgoBacktestPage;
