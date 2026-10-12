// import React from "react";

import CardLineChart from "components/Cards/CardLineChart.js";
import CardBarChart from "components/Cards/CardBarChart.js";
// import CardPageVisits from "components/Cards/CardPageVisits.js";
// import CardSocialTraffic from "components/Cards/CardSocialTraffic.js";
import useEvmGlobalContext from "context/EvmGlobalContext";
import CardBase from "components/Cards/CardBase";
import useHelper from "hooks/useHelper";
import CardInfo from "components/Cards/CardInfo";
import CardBarChartTrade from "components/Cards/CardBarChartTrade";

const Dashboard = () => {
    const { formatNumberDevise } = useHelper();
    const { ecudorState, investmentState }: any =
        useEvmGlobalContext();
    const {
        ecudor: { address: ecudorAddress, balanceECOR: ecudorBalanceECOR },
    } = ecudorState;
    const {
        investment: {
            address: investmentAddress,
            balanceECOR: investmentBalanceECOR,
        },
    } = investmentState;

    return (
        <>
            <div className="flex flex-wrap">
                <div className="relative w-full lg:w-8/12 px-4">
                    <div className="relative w-full mb-8">
                        {/* <input
                            disabled
                            name="coming-soon"
                            placeholder="coming soon"
                            className="absolute text-center bg-black-app text-white px-8 py-4 top-1/2 left-1/2 translate-x-[-50%] translate-y-[-50%] z-50 blur-none rounded"
                        /> */}
                        {/* <div className="bg-slate-500 static h-fit w-full z-40 blur-sm backdrop-blur-sm"> */}
                            <CardLineChart />
                        {/* </div> */}
                    </div>
                    <div className="relative w-full mb-10">
                        {/* <input
                            disabled
                            name="coming-soon"
                            placeholder="coming soon"
                            className="absolute text-center bg-black-app text-white px-8 py-4 top-1/2 left-1/2 translate-x-[-50%] translate-y-[-50%] z-50 blur-none rounded"
                        /> */}
                        {/* <div className="bg-slate-500 static h-fit w-full z-40 blur-sm backdrop-blur-sm"> */}
                            <CardBarChartTrade />
                        {/* </div> */}
                    </div>
                    <div className="relative w-full mb-10">
                        <input
                            disabled
                            name="coming-soon"
                            placeholder="coming soon"
                            className="absolute text-center bg-black-app text-white px-8 py-4 top-1/2 left-1/2 translate-x-[-50%] translate-y-[-50%] z-50 blur-none rounded"
                        />
                        <div className="bg-slate-500 static h-fit w-full z-40 blur-sm backdrop-blur-sm">
                            <CardBarChart />
                        </div>
                    </div>
                </div>

                <div className="w-full lg:w-4/12 px-4 mb-4">
                    <CardInfo />
                </div>
            </div>
            <div className="">
                <div className="flex flex-wrap">
                    <div className="w-full xl:w-6/12 px-4">
                        <CardBase
                            baseSubtitle="Ecudor Token Contract Address"
                            // baseTitle="Address"
                            baseDescripiron={ecudorAddress}
                        />
                    </div>
                    <div className="w-full xl:w-6/12 px-4">
                        <CardBase
                            baseSubtitle="Investment Contract Address"
                            // baseTitle="Address"
                            baseDescripiron={investmentAddress}
                        />
                    </div>
                    <div className="w-full xl:w-6/12 px-4">
                        <CardBase
                            baseSubtitle="Ecudor Token Total supply"
                            // baseTitle="Total supply"
                            baseDescripiron={formatNumberDevise(
                                ecudorBalanceECOR,
                                "ECOR"
                            )}
                        />
                    </div>
                    <div className="w-full xl:w-6/12 px-4">
                        <CardBase
                            baseSubtitle="Investment Contract Balance"
                            // baseTitle="Available tokens"
                            baseDescripiron={formatNumberDevise(
                                investmentBalanceECOR,
                                "ECOR"
                            )}
                        />
                    </div>
                </div>
            </div>
            <div className="flex flex-wrap mt-4">
                <div className="w-full xl:w-8/12 mb-12 xl:mb-0 px-4">
                    {/* <CardPageVisits /> */}
                </div>
                <div className="w-full xl:w-4/12 px-4">
                    {/* <CardSocialTraffic /> */}
                </div>
            </div>
        </>
    );
};

export default Dashboard;
