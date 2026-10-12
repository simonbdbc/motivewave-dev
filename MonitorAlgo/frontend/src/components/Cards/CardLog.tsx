import React from "react";

type Props = {
    logRef: React.RefObject<HTMLPreElement | null>;
};

const CardLog: React.FC<Props> = ({ logRef }) => (
    <div className="relative flex flex-col w-full h-full max-h-full mb-6 shadow-lg rounded-lg bg-white border-0">
        {/* Header prend sa taille naturelle */}
        <div className="rounded-t bg-white px-6 py-6">
            <div className="text-center flex justify-between">
                <h6 className="text-slate-700 text-xl font-bold">Log</h6>
            </div>
        </div>
        <hr className="border-b-1 border-slate-300" />
        {/* Le contenu remplit l'espace restant */}
        <div className="flex-1 flex flex-col min-h-0">
            <pre
                ref={logRef}
                className="flex-1 min-h-0 bg-black text-green-300 p-4 rounded-b-lg overflow-auto"
            >
                Sélectionnez un fichier…
            </pre>
        </div>
    </div>
);

export default CardLog;
