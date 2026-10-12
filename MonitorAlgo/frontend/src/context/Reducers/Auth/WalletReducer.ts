const WalletInitialState = {
    account: {
        address: "Connect Wallet",
        balanceECOR: "",
        balanceJEUR: "",
        allowanceAmountJEUR: "",
        isLoading: false,
        isLoaded: false,
        isConnected: false,
        error: "",
    },
    network: {
        id: "",
        name: "",
        isLoading: false,
        isLoaded: false,
        isConnected: false,
        error: "",
    },
};

const WalletReducer = (state: any, action: any) => {
    const { payload, type, field } = action;
    switch (type) {
        case "account_field": {
            return {
                ...state,
                account: {
                    ...state.account,
                    [field]: payload,
                },
            };
        }
        case "account_loading": {
            return {
                ...state,
                account: {
                    ...state.account,
                    isLoading: true,
                    isLoaded: false,
                    error: "",
                },
            };
        }
        case "account_loaded": {
            return {
                ...state,
                account: {
                    ...state.account,
                    isLoading: false,
                    isLoaded: true,
                },
            };
        }
        case "account_connected": {
            return {
                ...state,
                account: {
                    ...state.account,
                    isConnected: true,
                },
            };
        }
        case "account_error": {
            return {
                ...state,
                account: {
                    ...state.account,
                    isLoading: false,
                    isLoaded: false,
                    isConnected: false,
                    error: payload ? payload : "Error account.",
                },
            };
        }
        case "account_disconnected": {
            return {
                ...state,
                account: {
                    ...state.account,
                    address: "",
                    balanceECOR: "",
                    balanceJEUR: "",
                    allowanceAmountJEUR: "",
                    isConnected: false,
                },
            };
        }
        case "network_field": {
            return {
                ...state,
                network: {
                    ...state.network,
                    [field]: payload,
                },
            };
        }
        case "network_loading": {
            return {
                ...state,
                network: {
                    ...state.network,
                    isLoading: true,
                    isLoaded: false,
                    error: "",
                },
            };
        }
        case "network_loaded": {
            return {
                ...state,
                network: {
                    ...state.network,
                    isLoading: false,
                    isLoaded: true,
                },
            };
        }
        case "network_connected": {
            return {
                ...state,
                network: {
                    ...state.network,
                    isConnected: true,
                },
            };
        }
        case "network_error": {
            return {
                ...state,
                network: {
                    ...state.network,
                    isLoading: false,
                    isLoaded: false,
                    isConnected: false,
                    error: payload ? payload : "Error network.",
                },
            };
        }
        case "network_disconnected": {
            return {
                ...state,
                network: {
                    ...state.network,
                    id: "",
                    name: "",
                    isConnected: false,
                },
            };
        }
        default:
            return state;
    }
};

export { WalletInitialState };
export default WalletReducer;
