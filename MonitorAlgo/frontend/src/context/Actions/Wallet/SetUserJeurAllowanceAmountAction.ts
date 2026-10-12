const SetUserJeurAllowanceAmountAction = (dispatch: any, data: any) => {
    dispatch({
        type: "account_field",
        field: "allowanceAmountJEUR",
        payload: data,
    });
    dispatch({
        type: "account_loaded",
    });
};

export default SetUserJeurAllowanceAmountAction;
