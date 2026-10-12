const SetInvestmentContractAddressAction = (dispatch:any, data:any) => {
  dispatch({
    type: "investment_field",
    field: "address",
    payload: data,
  });
  dispatch({
    type: "investment_loaded",
  });
};

export default SetInvestmentContractAddressAction;
