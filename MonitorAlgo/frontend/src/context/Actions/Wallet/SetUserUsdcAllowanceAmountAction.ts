const SetUserUsdcAllowanceAmountAction = (dispatch: any, data: any) => {
    dispatch({
        type: "account_field",
        field: "allowanceAmountUSDC",
        payload: data,
    });
    dispatch({
        type: "account_loaded",
    });
};

export default SetUserUsdcAllowanceAmountAction;
