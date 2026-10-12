const UsdcInitialState = {
    usdc: {
        contract: null,
        address: "",
        balanceUSDC: "",
        isLoading: false,
        isLoaded: false,
        error: "",
    },
};

const UsdcReducer = (state: any, action: any) => {
    const { payload, type, field } = action;
    switch (type) {
        case "usdc_field": {
            return {
                ...state,
                usdc: {
                    ...state.usdc,
                    [field]: payload,
                },
            };
        }
        case "usdc_loading": {
            return {
                ...state,
                usdc: {
                    ...state.usdc,
                    isLoading: true,
                    isLoaded: false,
                    error: "",
                },
            };
        }
        case "usdc_loaded": {
            return {
                ...state,
                usdc: {
                    ...state.usdc,
                    isLoading: false,
                    isLoaded: true,
                    error: "",
                },
            };
        }
        case "usdc_error": {
            return {
                ...state,
                usdc: {
                    ...state.usdc,
                    isLoading: false,
                    isLoaded: false,
                    error: payload ? payload : "Error usdc Contract.",
                },
            };
        }
        default:
            return state;
    }
};

export { UsdcInitialState };
export default UsdcReducer;
