const InvestmentInitialState = {
    investment: {
        contract: null,
        address: "",
        balanceECOR: "",
        balanceJEUR: "",
        isLoading: false,
        isLoaded: false,
        error: "",
    },
};

const InvestmentReducer = (state: any, action: any) => {
    const { payload, type, field } = action;
    switch (type) {
        case "investment_field": {
            return {
                ...state,
                investment: {
                    ...state.investment,
                    [field]: payload,
                },
            };
        }
        case "investment_loading": {
            return {
                ...state,
                investment: {
                    ...state.investment,
                    isLoading: true,
                    isLoaded: false,
                    error: "",
                },
            };
        }
        case "investment_loaded": {
            return {
                ...state,
                investment: {
                    ...state.investment,
                    isLoading: false,
                    isLoaded: true,
                    error: "",
                },
            };
        }
        case "investment_error": {
            return {
                ...state,
                investment: {
                    ...state.investment,
                    isLoading: false,
                    isLoaded: false,
                    error: payload ? payload : "Error investment Contract.",
                },
            };
        }
        default:
            return state;
    }
};

export { InvestmentInitialState };
export default InvestmentReducer;
