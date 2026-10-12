// import React from "react";

import useEvmGlobalContext from "context/EvmGlobalContext";
import useHelper from "hooks/useHelper";

import CardInvestment from "components/Cards/CardInvestment";
import CardInfo from "components/Cards/CardInfo";
// import useInvestment from "hooks/useInvestment";
import CardTableInvestment from "components/Cards/CardTableInvestment";

const Investment = () => {
    const { formatNumberDevise } = useHelper();
    // const { buyEcudorToken, setApprovalAmount } = useInvestment();
    const { walletState, investmentState }: any = useEvmGlobalContext();
    const {
        account: { allowanceAmountJEUR },
    } = walletState;
    const {
        investment: { balanceECOR: investmentBalanceECOR },
    } = investmentState;
    return (
        <>
            <div className="flex flex-wrap">
                <div className="w-full lg:w-8/12 px-4">
                    <CardInvestment
                        investmentContractBalanceECOR={formatNumberDevise(
                            investmentBalanceECOR,
                            "ECOR"
                        )}
                        userAllowanceAmountJEUR={formatNumberDevise(
                            allowanceAmountJEUR,
                            "jEUR"
                        )}
                        // buyEcudorToken={buyEcudorToken}
                        // setApprovalAmount={setApprovalAmount}
                    />
                </div>
                <div className="w-full lg:w-4/12 px-4">
                    <CardInfo />
                </div>
                <CardTableInvestment />
            </div>
        </>
    );
};

export default Investment;
