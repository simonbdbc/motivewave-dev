const EcudorInitialState = {
    ecudor: {
        contract: null,
        address: "",
        owner: "",
        balanceECOR: "",
        isLoading: false,
        isLoaded: false,
        error: "",
    },
};

const EcudorReducer = (state: any, action: any) => {
    const { payload, type, field } = action;
    switch (type) {
        case "ecudor_field": {
            return {
                ...state,
                ecudor: {
                    ...state.ecudor,
                    [field]: payload,
                },
            };
        }
        case "ecudor_loading": {
            return {
                ...state,
                ecudor: {
                    ...state.ecudor,
                    isLoading: true,
                    isLoaded: false,
                    error: "",
                },
            };
        }
        case "ecudor_loaded": {
            return {
                ...state,
                ecudor: {
                    ...state.ecudor,
                    isLoading: false,
                    isLoaded: true,
                    error: "",
                },
            };
        }
        case "ecudor_error": {
            return {
                ...state,
                ecudor: {
                    ...state.ecudor,
                    isLoading: false,
                    isLoaded: false,
                    error: payload ? payload : "Error ecudor Contract.",
                },
            };
        }
        default:
            return state;
    }
};

export { EcudorInitialState };
export default EcudorReducer;
