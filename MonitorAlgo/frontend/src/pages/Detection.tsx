import CardTable from "components/Cards/CardTable";
import React, { useEffect, useState } from "react";

interface Pattern {
    id: number;
    pattern: string;
    message: string;
    active: boolean;
}

const Detection: React.FC = () => {
    const [patterns, setPatterns] = useState<Pattern[]>([]);
    const [form, setForm] = useState<Omit<Pattern, "id">>({
        pattern: "",
        message: "",
        active: true,
    });

    useEffect(() => {
        fetch("/api/alert-patterns")
            .then((r) => r.json())
            .then(setPatterns);
    }, []);

    function savePattern(e: React.FormEvent) {
        e.preventDefault();
        fetch("/api/alert-patterns", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(form),
        })
            .then((r) => r.json())
            .then((newPattern) => setPatterns([...patterns, newPattern]));
    }

    function deletePattern(id: number) {
        fetch(`/api/alert-patterns/${id}`, { method: "DELETE" }).then(() =>
            setPatterns(patterns.filter((p) => p.id !== id))
        );
    }

    return (
        <>
            <div className="flex flex-wrap mt-4">
                <div className="w-full mb-12 px-4">
                    <CardTable />
                </div>
                <div className="w-full mb-12 px-4">
                    <CardTable color="dark" />
                </div>
            </div>
            <div className="max-w-3xl mx-auto">
                <h2 className="text-lg font-bold mb-4">Motifs de détection</h2>
                <form
                    className="mb-6 flex flex-wrap gap-4 items-center"
                    onSubmit={savePattern}
                >
                    <input
                        className="border p-2 rounded"
                        required
                        name="regex-motif"
                        placeholder="Regex motif"
                        value={form.pattern}
                        onChange={(e) =>
                            setForm({ ...form, pattern: e.target.value })
                        }
                    />
                    <input
                        className="border p-2 rounded"
                        required
                        name="message-email"
                        placeholder="Message email"
                        value={form.message}
                        onChange={(e) =>
                            setForm({ ...form, message: e.target.value })
                        }
                    />
                    <label className="flex items-center gap-1">
                        <input
                            type="checkbox"
                            name="checkbox-actif"
                            checked={form.active}
                            onChange={(e) =>
                                setForm({ ...form, active: e.target.checked })
                            }
                        />{" "}
                        Actif
                    </label>
                    <button
                        className="bg-blue-600 text-white px-4 py-2 rounded"
                        type="submit"
                    >
                        Ajouter
                    </button>
                </form>
                <table className="w-full border">
                    <thead>
                        <tr>
                            <th className="border px-2 py-1">Motif (regex)</th>
                            <th className="border px-2 py-1">Message</th>
                            <th className="border px-2 py-1">Actif</th>
                            <th className="border px-2 py-1">Suppr</th>
                        </tr>
                    </thead>
                    <tbody>
                        {patterns.map((p) => (
                            <tr key={p.id}>
                                <td className="border px-2">{p.pattern}</td>
                                <td className="border px-2">{p.message}</td>
                                <td className="border px-2">
                                    {p.active ? "✅" : "❌"}
                                </td>
                                <td className="border px-2">
                                    <button
                                        onClick={() => deletePattern(p.id)}
                                        className="text-red-600"
                                    >
                                        ✖
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </>
    );
};

export default Detection;
