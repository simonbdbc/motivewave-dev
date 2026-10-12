const SetEcudorContractOwnerAction = (dispatch:any, data:any) => {
  dispatch({
    type: "ecudor_field",
    field: "owner",
    payload: data,
  });
  dispatch({
    type: "ecudor_loaded",
  });
};

export default SetEcudorContractOwnerAction;
