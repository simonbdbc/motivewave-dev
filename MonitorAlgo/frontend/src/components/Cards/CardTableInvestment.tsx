// import React from "react";
// import PropTypes from "prop-types";

// // components

// import TableDropdown from "components/Dropdowns/TableDropdown.js";

// export default function CardTableInvestment({ color }) {
//   return (
//     <>
//       <div
//         className={
//           "relative flex flex-col min-w-0 break-words w-full mb-6 shadow-lg rounded " +
//           (color === "light" ? "bg-white" : "bg-sky-900 text-white")
//         }
//       >
//         <div className="rounded-t mb-0 px-4 py-3 border-0">
//           <div className="flex flex-wrap items-center">
//             <div className="relative w-full px-4 max-w-full flex-grow flex-1">
//               <h3
//                 className={
//                   "font-semibold text-lg " +
//                   (color === "light" ? "text-slate-700" : "text-white")
//                 }
//               >
//                 Transactions
//               </h3>
//             </div>
//           </div>
//         </div>
//         <div className="block w-full overflow-x-auto">
//           {/* Projects table */}
//           <table className="items-center w-full bg-transparent border-collapse">
//             <thead>
//               <tr>
//                 <th
//                   className={
//                     "px-6 align-middle border border-solid py-3 text-xs uppercase border-l-0 border-r-0 whitespace-nowrap font-semibold text-left " +
//                     (color === "light"
//                       ? "bg-slate-50 text-slate-500 border-slate-100"
//                       : "bg-sky-800 text-sky-300 border-sky-700")
//                   }
//                 >
//                   Link to transaction
//                 </th>
//                 <th
//                   className={
//                     "px-6 align-middle border border-solid py-3 text-xs uppercase border-l-0 border-r-0 whitespace-nowrap font-semibold text-left " +
//                     (color === "light"
//                       ? "bg-slate-50 text-slate-500 border-slate-100"
//                       : "bg-sky-800 text-sky-300 border-sky-700")
//                   }
//                 >
//                   Amount
//                 </th>
//                 <th
//                   className={
//                     "px-6 align-middle border border-solid py-3 text-xs uppercase border-l-0 border-r-0 whitespace-nowrap font-semibold text-left " +
//                     (color === "light"
//                       ? "bg-slate-50 text-slate-500 border-slate-100"
//                       : "bg-sky-800 text-sky-300 border-sky-700")
//                   }
//                 >
//                   Date
//                 </th>
//                 <th
//                   className={
//                     "px-6 align-middle border border-solid py-3 text-xs uppercase border-l-0 border-r-0 whitespace-nowrap font-semibold text-left " +
//                     (color === "light"
//                       ? "bg-slate-50 text-slate-500 border-slate-100"
//                       : "bg-sky-800 text-sky-300 border-sky-700")
//                   }
//                 >
//                   Link to approval
//                 </th>
//               </tr>
//             </thead>
//             <tbody>
//               <tr>
//                 <th className="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4 text-left flex items-center">
//                   <span
//                     className={
//                       "ml-3 font-bold " +
//                       +(color === "light" ? "text-slate-600" : "text-white")
//                     }
//                   >
//                     Anchor on hash
//                   </span>
//                 </th>
//                 <td className="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
//                   $2,500 USD
//                 </td>
//                 <td className="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
//                   <i className="fas fa-circle text-orange-500 mr-2"></i> pending
//                 </td>

//                 <td className="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
//                   <div className="flex items-center">
//                     <span className="mr-2">Anchor on hash</span>
//                   </div>
//                 </td>
//               </tr>
//             </tbody>
//           </table>
//         </div>
//       </div>
//     </>
//   );
// }

// CardTableInvestment.defaultProps = {
//   color: "light",
// };

// // CardTableInvestment.propTypes removed (TypeScript migration)
//   color: PropTypes.oneOf(["light", "dark"]),
// };
import React from "react";
// import TableDropdown from "components/Dropdowns/TableDropdown";

export type CardTableInvestmentProps = {
    color?: "light" | "dark";
};

const CardTableInvestment: React.FC<CardTableInvestmentProps> = ({
    color = "light",
}) => {
    return (
        <div
            className={
                "relative flex flex-col min-w-0 break-words w-full mb-6 shadow-lg rounded " +
                (color === "light" ? "bg-white" : "bg-sky-900 text-white")
            }
        >
            <div className="rounded-t mb-0 px-4 py-3 border-0">
                <div className="flex flex-wrap items-center">
                    <div className="relative w-full px-4 max-w-full flex-grow flex-1">
                        <h3
                            className={
                                "font-semibold text-lg " +
                                (color === "light"
                                    ? "text-slate-700"
                                    : "text-white")
                            }
                        >
                            Transactions
                        </h3>
                    </div>
                </div>
            </div>
            <div className="block w-full overflow-x-auto">
                <table className="items-center w-full bg-transparent border-collapse">
                    <thead>
                        <tr>
                            <th
                                className={
                                    "px-6 align-middle border border-solid py-3 text-xs uppercase border-l-0 border-r-0 whitespace-nowrap font-semibold text-left " +
                                    (color === "light"
                                        ? "bg-slate-50 text-slate-500 border-slate-100"
                                        : "bg-sky-800 text-sky-300 border-sky-700")
                                }
                            >
                                Link to transaction
                            </th>
                            <th
                                className={
                                    "px-6 align-middle border border-solid py-3 text-xs uppercase border-l-0 border-r-0 whitespace-nowrap font-semibold text-left " +
                                    (color === "light"
                                        ? "bg-slate-50 text-slate-500 border-slate-100"
                                        : "bg-sky-800 text-sky-300 border-sky-700")
                                }
                            >
                                Amount
                            </th>
                            <th
                                className={
                                    "px-6 align-middle border border-solid py-3 text-xs uppercase border-l-0 border-r-0 whitespace-nowrap font-semibold text-left " +
                                    (color === "light"
                                        ? "bg-slate-50 text-slate-500 border-slate-100"
                                        : "bg-sky-800 text-sky-300 border-sky-700")
                                }
                            >
                                Date
                            </th>
                            <th
                                className={
                                    "px-6 align-middle border border-solid py-3 text-xs uppercase border-l-0 border-r-0 whitespace-nowrap font-semibold text-left " +
                                    (color === "light"
                                        ? "bg-slate-50 text-slate-500 border-slate-100"
                                        : "bg-sky-800 text-sky-300 border-sky-700")
                                }
                            >
                                Link to approval
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <th className="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4 text-left flex items-center">
                                <span
                                    className={
                                        "ml-3 font-bold " +
                                        (color === "light"
                                            ? "text-slate-600"
                                            : "text-white")
                                    }
                                >
                                    Anchor on hash
                                </span>
                            </th>
                            <td className="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
                                $2,500 USD
                            </td>
                            <td className="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
                                <i className="fas fa-circle text-orange-500 mr-2"></i>{" "}
                                pending
                            </td>
                            <td className="border-t-0 px-6 align-middle border-l-0 border-r-0 text-xs whitespace-nowrap p-4">
                                <div className="flex items-center">
                                    <span className="mr-2">Anchor on hash</span>
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default CardTableInvestment;
