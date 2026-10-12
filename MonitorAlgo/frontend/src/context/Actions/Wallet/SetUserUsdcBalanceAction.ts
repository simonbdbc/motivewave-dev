const SetUserUsdcBalanceAction = (dispatch: any, data: any) => {
    dispatch({
        type: "account_field",
        field: "balanceUSDC",
        payload: data,
    });
    dispatch({
        type: "account_loaded",
    });
};

export default SetUserUsdcBalanceAction;
