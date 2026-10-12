const SetJeurContractAddressAction = (dispatch:any, data:any) => {
  dispatch({
    type: "jeur_field",
    field: "address",
    payload: data,
  });
  dispatch({
    type: "jeur_loaded",
  });
};

export default SetJeurContractAddressAction;
