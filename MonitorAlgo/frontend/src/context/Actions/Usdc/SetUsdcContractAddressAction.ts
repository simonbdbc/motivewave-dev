const SetUsdcContractAddressAction = (dispatch:any, data:any) => {
  dispatch({
    type: "usdc_field",
    field: "address",
    payload: data,
  });
  dispatch({
    type: "usdc_loaded",
  });
};

export default SetUsdcContractAddressAction;
