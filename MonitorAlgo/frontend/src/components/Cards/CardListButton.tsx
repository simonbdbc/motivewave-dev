import React, { useState } from "react";
import useLoaderContext from "context/LoaderContext";
import toast from "react-hot-toast";

interface Props {
    apiCalls: {
        label: string;
        path: string;
    }[];
    listName: string;
}

const CardListButton: React.FC<Props> = ({ apiCalls, listName }) => {
    const { setLoader } = useLoaderContext();
    const [loading, setLoading] = useState<string | null>(null);

    const handleAction = async (path: string, label: string) => {
        setLoading(label);
        setLoader(true);
        try {
            const res = await fetch(path, { method: "POST" });
            if (!res.ok) throw new Error(`Erreur ${res.status}`);
            const text = await res.text();
            toast.success(`✅ ${label} : ${text}`);
        } catch (e: any) {
            toast.error(`❌ ${label} : ${e.message || "Erreur inconnue"}`);
        } finally {
            setLoader(false);
            setLoading(null);
        }
    };
    return (
        <div className="relative flex flex-col min-w-0 break-words w-full mb-6 shadow-lg rounded-lg bg-white border-0">
            <div className="rounded-t bg-white px-6 py-6">
                <div className="text-center flex justify-between">
                    <h6 className="text-slate-700 text-xl font-bold">
                        {listName}
                    </h6>
                </div>
            </div>
            <hr className="border-b-1 border-slate-300 mb-3" />
            {/* Partie scrollable */}
            <div className="flex-1 min-h-0 flex flex-col px-6 py-4 overflow-auto">
                {apiCalls.map(({ label, path }) => (
                    <button
                        key={path}
                        type="button"
                        className={`btn w-full mb-3 px-4 py-2 font-medium rounded
              ${
                  loading === label
                      ? "bg-gray-300 text-gray-500"
                      : "bg-secondary hover:bg-secondary-dark text-white"
              }
            `}
                        disabled={!!loading}
                        onClick={() => handleAction(path, label)}
                    >
                        {loading === label ? "En cours..." : label}
                    </button>
                ))}
            </div>
        </div>
    );
};

export default CardListButton;
