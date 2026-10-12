// import React from "react";
import toast, { Toaster, ToastBar } from "react-hot-toast";
import { CloseIcon } from "icons";

const HotToast = () => {
  return (
    <Toaster
      toastOptions={{
        success: {
          duration: 5000, // 5sec
        },
        error: {
          duration: 5000, // 5sec
        },
      }}
    >
      {(t) => (
        <ToastBar toast={t}>
          {({ icon, message }) => {
            // console.log(t);
            return (
              <>
                {icon}
                {message}
                {t.visible && t.type !== "loading" && (
                  <div className="flex border-l border-gray-200">
                    <button
                      onClick={() => toast.dismiss(t.id)}
                      className="w-full border border-transparent rounded-none rounded-r-lg pl-2 flex items-center justify-center text-sm font-medium text-secondary hover:text-secondary-dark"
                    >
                      <CloseIcon className="w-6 h-6" aria-hidden="true" />
                    </button>
                  </div>
                )}
              </>
            );
          }}
        </ToastBar>
      )}
    </Toaster>
  );
};

export default HotToast;
