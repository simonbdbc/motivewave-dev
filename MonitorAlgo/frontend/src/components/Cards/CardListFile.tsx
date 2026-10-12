import React from "react";
import { type FileNode } from "interfaces/types";
import ButtonListItem from "components/Buttons/ButtonListItem";

interface Props {
    nodes: FileNode[];
    selected: string | null;
    onSelect: (path: string) => void;
    listName: string;
}

const CardListFile: React.FC<Props> = ({
    nodes,
    selected,
    onSelect,
    listName,
}) => (
    <div className="relative flex flex-col min-w-0 break-words w-full h-full mb-6 shadow-lg rounded-lg bg-white border-0">
        <div className="rounded-t bg-white px-6 py-6">
            <div className="text-center flex justify-between">
                <h6 className="text-slate-700 text-xl font-bold">{listName}</h6>
            </div>
        </div>
        <hr className="border-b-1 border-slate-300" />
        {/* Partie scrollable */}
        <div className="flex-1 min-h-0 flex flex-col px-6 py-4 overflow-auto">
            <ButtonListItem
                nodes={nodes}
                selected={selected}
                onSelect={onSelect}
            />
        </div>
    </div>
);

export default CardListFile;
