// import React from "react";
// import Chart from "chart.js/auto";

// export default function CardBarChart() {
//   React.useEffect(() => {
//     if (window.myBar) {
//       window.myBar.destroy();
//     }
//     let config = {
//       type: "bar",
//       data: {
//         labels: [
//           "Jan",
//           "Feb",
//           "Mar",
//           "Apr",
//           "May",
//           "Jun",
//           "Jul",
//           "Aug",
//           "Sep",
//           "Oct",
//           "Nov",
//           "Dec",
//         ],
//         datasets: [
//           {
//             label: new Date().getFullYear(),
//             backgroundColor: "#ed64a6",
//             borderColor: "#ed64a6",
//             data: [30, 78, 56, 34, 100, 45, 13, 30, 78, 56, 34, 100],
//             fill: false,
//             barThickness: 8,
//           },
//           {
//             label: new Date().getFullYear() - 1,
//             fill: false,
//             backgroundColor: "#4c51bf",
//             borderColor: "#4c51bf",
//             data: [27, 68, 86, 74, 10, 4, 87, 86, 74, 10, 4, 87, 70],
//             barThickness: 8,
//           },
//         ],
//       },
//       options: {
//         maintainAspectRatio: false,
//         responsive: true,
//         title: {
//           display: false,
//           text: "Orders Chart",
//         },
//         tooltips: {
//           mode: "index",
//           intersect: false,
//         },
//         hover: {
//           mode: "nearest",
//           intersect: true,
//         },
//         legend: {
//           labels: {
//             fontColor: "rgba(0,0,0,.4)",
//           },
//           align: "end",
//           position: "bottom",
//         },
//         scales: {
//           x: {
//             display: false,
//             scaleLabel: {
//               display: true,
//               labelString: "Month",
//             },
//             gridLines: {
//               borderDash: [2],
//               borderDashOffset: [2],
//               color: "rgba(33, 37, 41, 0.3)",
//               zeroLineColor: "rgba(33, 37, 41, 0.3)",
//               zeroLineBorderDash: [2],
//               zeroLineBorderDashOffset: [2],
//             },
//           },
//           y: {
//             display: true,
//             scaleLabel: {
//               display: false,
//               labelString: "Value",
//             },
//             gridLines: {
//               borderDash: [2],
//               drawBorder: false,
//               borderDashOffset: [2],
//               color: "rgba(33, 37, 41, 0.2)",
//               zeroLineColor: "rgba(33, 37, 41, 0.15)",
//               zeroLineBorderDash: [2],
//               zeroLineBorderDashOffset: [2],
//             },
//           },
//         },
//       },
//     };
//     let ctx = document.getElementById("bar-chart").getContext("2d");
//     window.myBar = new Chart(ctx, config);
//   }, []);
//   return (
//     <>
//       <div className="relative flex flex-col min-w-0 break-words bg-white w-full mb-6 shadow-lg rounded">
//         <div className="rounded-t mb-0 px-4 py-3 bg-transparent">
//           <div className="flex flex-wrap items-center">
//             <div className="relative w-full max-w-full flex-grow flex-1">
//               <h6 className="uppercase text-slate-400 mb-1 text-xs font-semibold">
//                 Overview
//               </h6>
//               <h2 className="text-slate-700 text-xl font-semibold">
//                 Total asics
//               </h2>
//             </div>
//           </div>
//         </div>
//         <div className="p-4 flex-auto">
//           {/* Chart */}
//           <div className="relative h-350-px">
//             <canvas id="bar-chart"></canvas>
//           </div>
//         </div>
//       </div>
//     </>
//   );
// }

import { useRef, useEffect } from "react";
import Chart from "chart.js/auto";

export default function CardBarChartTrade() {
    const chartRef = useRef<HTMLCanvasElement | null>(null);
    const chartInstance = useRef<Chart | null>(null);

    useEffect(() => {
        if (chartInstance.current) {
            chartInstance.current.destroy();
        }
        if (!chartRef.current) return;

        const config: any = {
            type: "bar",
            data: {
                labels: [
                    "Jan",
                    "Feb",
                    "Mar",
                    "Apr",
                    "May",
                    "Jun",
                    "Jul",
                    "Aug",
                    "Sep",
                    "Oct",
                    "Nov",
                    "Dec",
                ],
                datasets: [
                    {
                        label: "LOSS",
                        backgroundColor: "#ed64a6",
                        borderColor: "#ed64a6",
                        data: [
                            30, 78, 56, 34, 100, 45, 13, 30, 78, 56, 34, 100,
                        ],
                        barThickness: 8,
                    },
                    {
                        label: "WIN",
                        backgroundColor: "#4c51bf",
                        borderColor: "#4c51bf",
                        data: [27, 68, 86, 74, 10, 4, 87, 86, 74, 10, 4, 87],
                        barThickness: 8,
                    },
                ],
            },
            options: {
                maintainAspectRatio: false,
                responsive: true,
                plugins: {
                    legend: {
                        labels: {
                            color: "rgba(0,0,0,.4)",
                        },
                        align: "end",
                        position: "bottom",
                    },
                    title: {
                        display: false,
                        text: "Orders Chart",
                    },
                    tooltip: {
                        mode: "index" as const,
                        intersect: false,
                    },
                },
                hover: {
                    mode: "nearest",
                    intersect: true,
                },
                scales: {
                    x: {
                        display: true,
                        title: {
                            display: false,
                            text: "Month",
                        },
                        grid: {
                            borderDash: [2],
                            color: "rgba(33, 37, 41, 0.3)",
                            zeroLineColor: "rgba(33, 37, 41, 0.3)",
                            zeroLineBorderDash: [2],
                        },
                    },
                    y: {
                        display: true,
                        title: {
                            display: false,
                            text: "Value",
                        },
                        grid: {
                            borderDash: [2],
                            drawBorder: false,
                            color: "rgba(33, 37, 41, 0.2)",
                            zeroLineColor: "rgba(33, 37, 41, 0.15)",
                            zeroLineBorderDash: [2],
                        },
                    },
                },
            },
        };

        // @ts-ignore pour passer le typage strict (peut s'améliorer selon Chart.js)
        chartInstance.current = new Chart(chartRef.current, config);

        // Nettoyage (unmount)
        return () => {
            if (chartInstance.current) {
                chartInstance.current.destroy();
            }
        };
    }, []);

    return (
        <div className="relative flex flex-col min-w-0 break-words bg-white w-full mb-6 shadow-lg rounded">
            <div className="rounded-t mb-0 px-4 py-3 bg-transparent">
                <div className="flex flex-wrap items-center">
                    <div className="relative w-full max-w-full flex-grow flex-1">
                        <h6 className="uppercase text-slate-400 mb-1 text-xs font-semibold">
                            Overview
                        </h6>
                        <h2 className="text-slate-700 text-xl font-semibold">
                            Total trades
                        </h2>
                    </div>
                </div>
            </div>
            <div className="p-4 flex-auto">
                {/* Chart */}
                <div className="relative h-350-px">
                    <canvas ref={chartRef} />
                </div>
            </div>
        </div>
    );
}
