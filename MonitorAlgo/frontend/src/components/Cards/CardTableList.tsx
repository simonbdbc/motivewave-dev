import React from "react";

export type CardTableListProps = {
    color?: "light" | "dark";
    tableName: string;
    columns?: string[]; // optionnel : ordre des colonnes voulu, sinon auto
    rows: Array<Record<string, any>>;
};

const CardTableList: React.FC<CardTableListProps> = ({
    color = "light",
    tableName,
    columns,
    rows,
}) => {
    // Colonnes auto si non fournies
    const actualColumns =
        columns && columns.length > 0
            ? columns
            : rows.length > 0
            ? Object.keys(rows[0])
            : [];

    return (
        <div
            className={
                "relative flex flex-col min-w-0 break-words w-full mb-6 shadow-lg rounded overflow-y-auto max-h-[80vh] " +
                (color === "light" ? "bg-white" : "bg-sky-900 text-white")
            }
        >
            <div className="rounded-t mb-0 px-4 py-3 border-0">
                <div className="flex flex-wrap items-center">
                    <div className="relative w-full px-4 max-w-full flex-grow flex-1">
                        <h3
                            className={
                                "font-semibold text-lg " +
                                (color === "light"
                                    ? "text-slate-700"
                                    : "text-white")
                            }
                        >
                            {tableName}
                        </h3>
                    </div>
                </div>
            </div>
            <div className="block w-full overflow-x-auto">
                <table className="items-center w-full bg-transparent border-collapse">
                    <thead>
                        <tr>
                            {actualColumns.map((col) => (
                                <th
                                    key={col}
                                    className={
                                        "sticky top-0 px-6 align-middle border-solid py-3 text-xs uppercase border-0 whitespace-nowrap font-semibold text-left " +
                                        (color === "light"
                                            ? "bg-slate-50 text-slate-500 border-slate-100"
                                            : "bg-sky-800 text-sky-300 border-sky-700")
                                    }
                                >
                                    {col.replace(/([a-z])([A-Z])/g, "$1 $2")}
                                </th>
                            ))}
                        </tr>
                    </thead>
                    <tbody>
                        {rows.length === 0 ? (
                            <tr>
                                <td
                                    colSpan={actualColumns.length}
                                    className="p-4 text-center text-slate-400"
                                >
                                    Aucune donnée
                                </td>
                            </tr>
                        ) : (
                            rows.map((row, idx) => (
                                <tr key={idx}>
                                    {actualColumns.map((col) => (
                                        <td
                                            key={col}
                                            className="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4"
                                        >
                                            {row[col] !== undefined &&
                                            row[col] !== null
                                                ? String(row[col])
                                                : ""}
                                        </td>
                                    ))}
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default CardTableList;
