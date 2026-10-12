import { createContext, useContext, useReducer } from "react";

import WalletReducer, {
    WalletInitialState,
} from "context/Reducers/Auth/WalletReducer";
import EcudorReducer, {
    EcudorInitialState,
} from "context/Reducers/Contract/EcudorReducer";
import InvestmentReducer, {
    InvestmentInitialState,
} from "context/Reducers/Contract/InvestmentReducer";
import RewardReducer, {
    RewardInitialState,
} from "context/Reducers/Contract/RewardReducer";
import JeurReducer, {
    JeurInitialState,
} from "context/Reducers/Contract/JeurReducer";

const EvmGlobalContext = createContext({});

const EvmGlobalContextProvider = ({ children }: any) => {
    const [walletState, walletDispatch] = useReducer(
        WalletReducer,
        WalletInitialState
    );
    const [ecudorState, ecudorDispatch] = useReducer(
        EcudorReducer,
        EcudorInitialState
    );
    const [jeurState, jeurDispatch] = useReducer(JeurReducer, JeurInitialState);
    const [investmentState, investmentDispatch] = useReducer(
        InvestmentReducer,
        InvestmentInitialState
    );
    const [rewardState, rewardDispatch] = useReducer(
        RewardReducer,
        RewardInitialState
    );

    const value = {
        walletState,
        walletDispatch,
        ecudorState,
        ecudorDispatch,
        jeurState,
        jeurDispatch,
        investmentState,
        investmentDispatch,
        rewardState,
        rewardDispatch,
    };

    return (
        <EvmGlobalContext.Provider value={value}>
            {children}
        </EvmGlobalContext.Provider>
    );
};

const useEvmGlobalContext = () => {
    const context = useContext(EvmGlobalContext);

    if (context === undefined) {
        throw new Error("useGlobalContext must be used within GlobalContext");
    }

    return context;
};

export { EvmGlobalContextProvider };
export default useEvmGlobalContext;
