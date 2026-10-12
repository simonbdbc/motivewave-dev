const SetEcudorContractAction = (dispatch:any, data:any) => {
  dispatch({
    type: "ecudor_field",
    field: "contract",
    payload: data,
  });
  dispatch({
    type: "ecudor_loaded",
  });
};

export default SetEcudorContractAction;
