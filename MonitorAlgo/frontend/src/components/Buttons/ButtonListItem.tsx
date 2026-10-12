import React from "react";
import { type FileNode } from "interfaces/types";

interface Props {
    nodes: FileNode[];
    selected: string | null;
    onSelect: (path: string) => void;
}

const ButtonListItem: React.FC<Props> = ({ nodes, selected, onSelect }) => (
    <ul className="w-full">
        {nodes
            .filter((node) => !node.isDirectory)
            .map((node) => (
                <li
                    key={node.path}
                    className="flex items-start mb-1 text-base leading-relaxed text-slate-700"
                >
                    <button
                        className={`text-left w-full truncate px-1 py-0.5 rounded ${
                            selected === node.path
                                ? "bg-secondary-light text-slate-700 font-semibold"
                                : "text-secondary-dark"
                        }`}
                        onClick={() => onSelect(node.path)}
                        title={node.name}
                    >
                       ⤷ {node.name}
                    </button>
                </li>
            ))}
    </ul>
);

export default ButtonListItem;
