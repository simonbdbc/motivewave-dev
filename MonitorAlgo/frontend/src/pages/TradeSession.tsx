import CardTableList from "components/Cards/CardTableList";
import React from "react";
import { useFetchTable } from "hooks/useFetchTable";
import CardTableSelectList from "components/Cards/CardTableSelectList";

const TradeSessionPage: React.FC = () => {
    const { data: sessions } = useFetchTable("/api/sessions/last-three");
    const { data: trades } = useFetchTable("/api/session/read/all-trades");

    return (
        <>
            <div className="flex flex-wrap">
            <div className="w-full lg:w-12/12 px-4 pb-6">
                    <CardTableSelectList
                        tableName="Sessions"
                        rows={sessions}
                        color="light"
                    />
                </div>
                <div className="w-full lg:w-12/12 px-4 pb-6">
                    <CardTableList
                        tableName="Trades"
                        rows={trades}
                        color="dark"
                    />
                </div>
            </div>
        </>
    );
};

export default TradeSessionPage;
