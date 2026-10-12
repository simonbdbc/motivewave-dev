const SetUserEcudorBalanceAction = (dispatch: any, data: any) => {
    dispatch({
        type: "account_field",
        field: "balanceECOR",
        payload: data,
    });
    dispatch({
        type: "account_loaded",
    });
};

export default SetUserEcudorBalanceAction;
