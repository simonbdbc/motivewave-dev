const SetInvestmentContractBalanceAction = (dispatch:any, data:any) => {
  dispatch({
    type: "investment_field",
    field: "balanceECOR",
    payload: data,
  });
  dispatch({
    type: "investment_loaded",
  });
};

export default SetInvestmentContractBalanceAction;
