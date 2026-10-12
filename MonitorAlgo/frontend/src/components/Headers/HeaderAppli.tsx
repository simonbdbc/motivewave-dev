// import React from "react";

// import useEvmGlobalContext from "context/EvmGlobalContext";
// import useHelper from "hooks/useHelper";

// import CardHeader from "components/Cards/CardHeader";

import FondPolygon from "assets/img/fond-polygon.svg";
// import { LogoTransparentIcon, JeurIcon, MoneyIcon } from "icons";

const HeaderAppli = () => {
    // const { formatNumberDevise } = useHelper();
    // const { walletState, ecudorState, investmentState }: any =
    //     useEvmGlobalContext();
    // const {
    //     account: { balanceECOR: userBalanceECOR, balanceJEUR: userBalanceJEUR },
    // } = walletState;
    // const {
    //     ecudor: { address: ecudorAddress, balanceECOR: ecudorBalanceECOR },
    // }: any = ecudorState;
    // const {
    //     investment: {
    //         address: investmentAddress,
    //         balanceECOR: investmentBalanceECOR,
    //     },
    // }: any = investmentState;

    return (
        <>
            {/* Header */}
            <div
                className="relative bg-secondary md:pt-[4.5rem] pb-32 pt-12"
                style={{
                    backgroundImage: `url(${FondPolygon})`,
                    backgroundRepeat: "no-repeat",
                    backgroundPosition: "center top",
                }}
            >
                <div className="px-4 md:px-10 mx-auto w-full">
                    <div>
                        {/* Card headers */}
                        {/* <div className="flex flex-wrap">
                            <div className="w-full lg:w-4/12 px-4">
                                <CardHeader
                                    headerSubtitle="My ECOR Balance"
                                    headerTitle={formatNumberDevise(
                                        userBalanceECOR,
                                        "ECOR"
                                    )}
                                    headerIconComponent={
                                        <LogoTransparentIcon
                                            className="w-6 h-6"
                                            aria-hidden="true"
                                        />
                                    }
                                    headerIconColor="bg-secondary"
                                />
                            </div>
                            <div className="w-full lg:w-4/12 px-4">
                                <CardHeader
                                    headerSubtitle="My jEUR Balance"
                                    headerTitle={formatNumberDevise(
                                        userBalanceJEUR,
                                        "jEUR"
                                    )}
                                    headerIconComponent={
                                        <JeurIcon
                                            className="w-6 h-6"
                                            aria-hidden="true"
                                        />
                                    }
                                    headerIconColor="bg-secondary"
                                />
                            </div>
                            <div className="w-full lg:w-4/12 px-4">
                                <CardHeader
                                    headerSubtitle="Estimated Earning"
                                    headerTitle="15% APR"
                                    headerIconComponent={
                                        <MoneyIcon
                                            className="w-6 h-6"
                                            aria-hidden="true"
                                        />
                                    }
                                    headerIconColor="bg-secondary"
                                />
                            </div>
                        </div> */}
                    </div>
                </div>
            </div>
        </>
    );
};

export default HeaderAppli;
