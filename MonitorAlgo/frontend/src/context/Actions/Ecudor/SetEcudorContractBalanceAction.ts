const SetEcudorContractBalanceAction = (dispatch:any, data:any) => {
  dispatch({
    type: "ecudor_field",
    field: "balanceECOR",
    payload: data,
  });
  dispatch({
    type: "ecudor_loaded",
  });
};

export default SetEcudorContractBalanceAction;
