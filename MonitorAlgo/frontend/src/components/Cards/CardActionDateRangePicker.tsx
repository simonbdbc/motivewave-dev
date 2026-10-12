import React, { useState } from "react";
import toast from "react-hot-toast";
import DateRangePicker from "components/Inputs/DateRangePicker";

interface Props {
    listName: string;
    apiCall: {
        label: string;
        path: string;
    };
    checkboxLabel: string;
    checkboxChecked: boolean;
    onAfterRun?: () => void;
}

function formatDate(date: string): string {
    // date : "mm/dd/yyyy"
    const [mm, dd, yyyy] = date.split("/");
    return `${yyyy}-${mm.padStart(2, "0")}-${dd.padStart(2, "0")}`;
}

const currentDate = new Date();
const currentDateFormatted = new Intl.DateTimeFormat("en-US").format(
    currentDate
);
const sevenDayLastDate = new Date();
sevenDayLastDate.setDate(currentDate.getDate() - 30);
const sevenDayLastFormatted = new Intl.DateTimeFormat("en-US").format(
    sevenDayLastDate
);

const CardActionDateRangePicker: React.FC<Props> = ({
    apiCall,
    listName,
    checkboxLabel,
    checkboxChecked,
    onAfterRun,
}) => {
    const [loading, setLoading] = useState<string | null>(null);

    const [startDate, setStartDate] = useState<string>(sevenDayLastFormatted);
    const [endDate, setEndDate] = useState<string>(currentDateFormatted);

    const [ignorePeriod, setIgnorePeriod] = useState<boolean>(checkboxChecked);

    const handleAction = async (path: string, label: string) => {
        setLoading(label);
        try {
            const body = ignorePeriod
                ? { dateFrom: null, dateTo: null }
                : {
                      dateFrom: formatDate(startDate),
                      dateTo: formatDate(endDate),
                  };

            const res = await fetch(path, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(body),
            });
            if (!res.ok) throw new Error(`Erreur ${res.status}`);
            const text = await res.text();
            if (onAfterRun) onAfterRun();
            toast.success(`✅ ${label} : ${text}`);
        } catch (e: any) {
            toast.error(`❌ ${label} : ${e.message || "Erreur inconnue"}`);
        } finally {
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
            <div className="flex-1 min-h-0 flex flex-col px-6 py-4 overflow-auto">
                <button
                    key={apiCall.path}
                    type="button"
                    className={`btn w-full mb-3 px-4 py-2 font-medium rounded
              ${
                  loading === apiCall.label
                      ? "bg-gray-300 text-gray-500"
                      : "bg-secondary hover:bg-secondary-dark text-white"
              }
            `}
                    disabled={!!loading}
                    onClick={() => handleAction(apiCall.path, apiCall.label)}
                >
                    {loading === apiCall.label ? "En cours..." : apiCall.label}
                </button>

                <div className="flex items-center mt-4">
                    <input
                        type="checkbox"
                        id="ignore-period"
                        checked={ignorePeriod}
                        onChange={() => setIgnorePeriod((v) => !v)}
                        className="hidden peer"
                    />
                    <label
                        htmlFor="ignore-period"
                        className="flex items-center w-full h-12 text-gray-app bg-white border-2 border-gray-200 rounded-lg cursor-pointer dark:hover:text-gray-300 dark:border-gray-700 peer-checked:border-secondary dark:peer-checked:border-secondary hover:text-gray-600 dark:peer-checked:text-gray-300 peer-checked:text-gray-600 hover:bg-gray-50 dark:text-gray-400 dark:bg-gray-800 dark:hover:bg-gray-700"
                    >
                        {/* Icon, peer-checked visible only if checked */}
                        <span
                            className={`mr-2 ${
                                ignorePeriod ? "text-secondary" : "gray-200"
                            } items-center`}
                        >
                            <svg
                                xmlns="http://www.w3.org/2000/svg"
                                width={20}
                                height={20}
                                fill="none"
                                viewBox="0 0 21 21"
                                className="w-12 h-12"
                            >
                                <path
                                    fill="currentColor"
                                    fillRule="evenodd"
                                    d="M16.707 7.293a1 1 0 0 1 0 1.414l-6 6a1 1 0 0 1-1.414 0l-3-3a1 1 0 0 1 1.414-1.414L10 12.586l5.293-5.293a1 1 0 0 1 1.414 0Z"
                                    clipRule="evenodd"
                                />
                            </svg>
                        </span>
                        <div>{checkboxLabel}</div>
                    </label>
                </div>

                <DateRangePicker
                    initialStartDate={sevenDayLastFormatted}
                    initialEndDate={currentDateFormatted}
                    openDefault={false}
                    disabled={ignorePeriod}
                    onChange={(start: string | null, end: string | null) => {
                        if (start) setStartDate(start);
                        if (end) setEndDate(end);
                    }}
                />
            </div>
        </div>
    );
};

export default CardActionDateRangePicker;
