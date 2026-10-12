const RewardInitialState = {
    reward: {
        contract: null,
        address: "",
        balanceECOR: "",
        balanceJEUR: "",
        isLoading: false,
        isLoaded: false,
        error: "",
    },
};

const RewardReducer = (state: any, action: any) => {
    const { payload, type, field } = action;
    switch (type) {
        case "field": {
            return {
                ...state,
                reward: {
                    ...state.reward,
                    [field]: payload,
                },
            };
        }
        case "reward_loading": {
            return {
                ...state,
                reward: {
                    ...state.reward,
                    isLoading: true,
                    isLoaded: false,
                    error: "",
                },
            };
        }
        case "reward_loaded": {
            return {
                ...state,
                reward: {
                    ...state.reward,
                    isLoading: false,
                    isLoaded: true,
                    error: "",
                },
            };
        }
        case "reward_error": {
            return {
                ...state,
                reward: {
                    ...state.reward,
                    isLoading: false,
                    isLoaded: false,
                    error: payload ? payload : "Error reward Contract.",
                },
            };
        }
        default:
            return state;
    }
};

export { RewardInitialState };
export default RewardReducer;
