// import React from "react";

const Spinner = () => {
  return (
    <>
      <div className="absolute h-full w-full z-50">
        <div className="h-full flex items-center justify-center">
          <div className="w-36 h-36 border-b-8 border-secondary-dark rounded-full animate-spin"></div>
        </div>
      </div>
    </>
  );
};

export default Spinner;
