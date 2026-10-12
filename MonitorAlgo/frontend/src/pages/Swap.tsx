// import React from "react";

import CardSwapMtPelerin from "components/Cards/CardSwapMtPelerin";
import CardInfo from "components/Cards/CardInfo";

const Swap = () => {
  return (
    <>
      <div className="flex flex-wrap">
        <div className="w-full lg:w-8/12 px-4">
          <CardSwapMtPelerin />
        </div>
        <div className="w-full lg:w-4/12 px-4">
          <CardInfo />
        </div>
      </div>
    </>
  );
};

export default Swap;
