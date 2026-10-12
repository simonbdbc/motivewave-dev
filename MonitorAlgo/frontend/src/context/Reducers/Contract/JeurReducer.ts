const JeurInitialState = {
    jeur: {
        contract: null,
        address: "",
        balanceJEUR: "",
        isLoading: false,
        isLoaded: false,
        error: "",
    },
};

const JeurReducer = (state: any, action: any) => {
    const { payload, type, field } = action;
    switch (type) {
        case "jeur_field": {
            return {
                ...state,
                jeur: {
                    ...state.jeur,
                    [field]: payload,
                },
            };
        }
        case "jeur_loading": {
            return {
                ...state,
                jeur: {
                    ...state.jeur,
                    isLoading: true,
                    isLoaded: false,
                    error: "",
                },
            };
        }
        case "jeur_loaded": {
            return {
                ...state,
                jeur: {
                    ...state.jeur,
                    isLoading: false,
                    isLoaded: true,
                    error: "",
                },
            };
        }
        case "jeur_error": {
            return {
                ...state,
                jeur: {
                    ...state.jeur,
                    isLoading: false,
                    isLoaded: false,
                    error: payload ? payload : "Error jeur Contract.",
                },
            };
        }
        default:
            return state;
    }
};

export { JeurInitialState };
export default JeurReducer;
