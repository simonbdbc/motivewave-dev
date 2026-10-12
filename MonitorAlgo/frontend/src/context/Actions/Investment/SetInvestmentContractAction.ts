const SetInvestmentContractAction = (dispatch:any, data:any) => {
  dispatch({
    type: "investment_field",
    field: "contract",
    payload: data,
  });
  dispatch({
    type: "investment_loaded",
  });
};

export default SetInvestmentContractAction;
