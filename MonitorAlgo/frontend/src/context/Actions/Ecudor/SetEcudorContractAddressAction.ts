const SetEcudorContractAddressAction = (dispatch:any, data:any) => {
  dispatch({
    type: "ecudor_field",
    field: "address",
    payload: data,
  });
  dispatch({
    type: "ecudor_loaded",
  });
};

export default SetEcudorContractAddressAction;
