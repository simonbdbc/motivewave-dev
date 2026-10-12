// import React from "react";

import CardInfo from "components/Cards/CardInfo";
import CardReward from "components/Cards/CardReward";

const Reward = () => {
  return (
    <>
      <div className="flex flex-wrap">
        <div className="relative w-full lg:w-8/12 px-4">
          <input
            disabled
            placeholder="coming soon"
            className="absolute text-center bg-black-app text-white px-8 py-4 top-1/2 left-1/2 translate-x-[-50%] translate-y-[-50%] z-50 blur-none rounded"
          />
          <div className="bg-slate-500 static h-fit w-full z-40 blur-sm backdrop-blur-sm">
            <CardReward />
          </div>
        </div>
        <div className="w-full lg:w-4/12 px-4">
          <CardInfo />
        </div>
      </div>
    </>
  );
};

export default Reward;
