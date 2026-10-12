const SetUserJeurBalanceAction = (dispatch: any, data: any) => {
    dispatch({
        type: "account_field",
        field: "balanceJEUR",
        payload: data,
    });
    dispatch({
        type: "account_loaded",
    });
};

export default SetUserJeurBalanceAction;
