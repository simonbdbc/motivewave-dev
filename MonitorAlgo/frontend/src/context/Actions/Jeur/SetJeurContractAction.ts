const SetJeurContractAction = (dispatch:any, data:any) => {
  dispatch({
    type: "jeur_field",
    field: "contract",
    payload: data,
  });
  dispatch({
    type: "jeur_loaded",
  });
};

export default SetJeurContractAction;
