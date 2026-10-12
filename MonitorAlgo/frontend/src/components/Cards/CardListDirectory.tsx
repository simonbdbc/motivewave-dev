import React from "react";
import { type FileNode } from "interfaces/types";
import ButtonListItem from "components/Buttons/ButtonListItem";

interface Props {
    nodes: FileNode[];
    selected: string | null;
    onSelect: (path: string) => void;
    openDirs: Record<string, boolean>;
    onToggle: (path: string) => void;
    listName: string;
}

const CardListDirectory: React.FC<Props> = ({
    nodes,
    selected,
    onSelect,
    openDirs,
    onToggle,
    listName,
}) => (
    <div className="relative flex flex-col min-w-0 break-words w-full h-full mb-6 shadow-lg rounded-lg bg-white border-0">
        <div className="rounded-t bg-white px-6 py-6">
            <div className="text-center flex justify-between">
                <h6 className="text-slate-700 text-xl font-bold">{listName}</h6>
            </div>
        </div>
        <hr className="border-b-1 border-slate-300" />
        <div className="flex-1 min-h-0 flex flex-col px-6 py-4 overflow-auto">
            <ul className="w-full">
                {nodes.map(
                    (node) => (
                        // node.isDirectory ? (
                        <li key={node.path}>
                            <div
                                className="font-bold cursor-pointer flex items-center gap-1 py-1"
                                onClick={() => onToggle(node.path)}
                            >
                                <span>{openDirs[node.path] ? "📂" : "📁"}</span>
                                <span>{node.name}</span>
                                <span>{openDirs[node.path] ? "▾" : "▸"}</span>
                            </div>
                            {openDirs[node.path] && node.children && (
                                <div className="ml-4 border-l border-slate-100 pl-2">
                                    {/* <CardListDirectory
                                        nodes={node.children}
                                        selected={selected}
                                        onSelect={onSelect}
                                        openDirs={openDirs}
                                        onToggle={onToggle}
                                        listName="" // pas de titre pour les sous-dossiers
                                    /> */}
                                    <ButtonListItem
                                        // nodes={nodes}
                                        nodes={node.children}
                                        selected={selected}
                                        onSelect={onSelect}
                                    />
                                </div>
                            )}
                        </li>
                    )
                    // ) : (
                    //     <li
                    //         key={node.path}
                    //         className="flex items-start mb-1 text-base leading-relaxed text-slate-700"
                    //     >
                    //         <ButtonListItem
                    //             selected={selected === node.path}
                    //             onClick={() => onSelect(node.path)}
                    //             title={node.name}
                    //         >
                    //             {node.name}
                    //         </ButtonListItem>
                    //     </li>
                    // )
                )}
            </ul>
        </div>
    </div>
);

export default CardListDirectory;
