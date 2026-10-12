import CardListButton from "components/Cards/CardListButton";
import CardTableList from "components/Cards/CardTableList";
import React from "react";
import { useFetchTable } from "hooks/useFetchTable";

const apiCalls1 = [
    { label: "Initialise All Tables", path: "/api/admin/init-db" },
    { label: "Delete All Tables", path: "/api/admin/reset-db" },
];
const apiCalls2 = [
    { label: "Seed Data", path: "/api/admin/seed-data" },
    {
        label: "Seed Patterns (very long process / +1h / 16 777 216 lines)",
        path: "/api/admin/seed-patterns",
    },
];

const AdminPage: React.FC = () => {
    const { data: instruments } = useFetchTable("/api/admin/read/instruments");
    const { data: directions } = useFetchTable("/api/admin/read/directions");
    const { data: renkoSizes } = useFetchTable("/api/admin/read/renko-sizes");
    const { data: stopLosses } = useFetchTable("/api/admin/read/stop-losses");
    const { data: takeProfits } = useFetchTable("/api/admin/read/take-profits");
    const { data: timeframes } = useFetchTable("/api/admin/read/timeframes");
    const { data: suites } = useFetchTable("/api/admin/read/suites");

    return (
        <>
            <div className="flex flex-wrap">
                <div className="w-full lg:w-6/12 px-4 pb-6">
                    <CardListButton
                        apiCalls={apiCalls1}
                        listName="Admin Database"
                    />
                </div>
                <div className="w-full lg:w-6/12 px-4 pb-6">
                    <CardListButton
                        apiCalls={apiCalls2}
                        listName="Feed Database"
                    />
                </div>
                <div className="w-full lg:w-4/12 px-4 pb-6">
                    <CardTableList
                        tableName="Instrument"
                        rows={instruments}
                        color="dark"
                    />
                    <CardTableList
                        tableName="Direction"
                        rows={directions}
                        color="dark"
                    />
                    <CardTableList
                        tableName="RenkoSize"
                        rows={renkoSizes}
                        color="dark"
                    />
                </div>
                <div className="w-full lg:w-4/12 px-4 pb-6">
                    <CardTableList
                        tableName="StopLoss"
                        rows={stopLosses}
                        color="dark"
                    />
                    <CardTableList
                        tableName="TakeProfit"
                        rows={takeProfits}
                        color="dark"
                    />
                    <CardTableList
                        tableName="Timeframe"
                        rows={timeframes}
                        color="dark"
                    />
                </div>
                <div className="w-full lg:w-4/12 px-4 pb-6">
                    <CardTableList
                        tableName="Suite"
                        rows={suites}
                        color="dark"
                    />
                </div>
            </div>
        </>
    );
};

export default AdminPage;
