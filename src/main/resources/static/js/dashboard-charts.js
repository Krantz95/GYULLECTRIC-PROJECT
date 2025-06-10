

const datasets = [
    { id: 'castingChart', statusId: 'castingStatus', warningId: 'castingWarning', label: '압력/온도 감지', value: 80 },
    { id: 'weldingChart', statusId: 'weldingStatus', warningId: 'weldingWarning', label: '용접 출력 감지', value: 20 }
];

// ================== 상단 2 : 공정별ok/ng ==================
// function drawChart(ctx, ok, ng, textElementId) {
//     const total = ok + ng;
//     const okPercent = total > 0 ? ((ok / total) * 100).toFixed(0) : 0;
//     const ngPercent = total > 0 ? ((ng / total) * 100).toFixed(0) : 0;
//
//     new Chart(ctx, {
//         type: 'doughnut',
//         data: {
//             labels: ['OK', 'NG'],
//             datasets: [{
//                 data: [ok, ng],
//                 backgroundColor: ['#4CAF50', '#F44336'],
//                 borderWidth: 0
//             }]
//         },
//         options: {
//             responsive: true,
//             maintainAspectRatio: true,
//             cutout: '40%',
//             plugins: {
//                 legend: { display: false }
//             }
//         }
//     });
//
//     // 퍼센트 텍스트 표시
//     const textDiv = document.getElementById(textElementId);
//     textDiv.innerHTML =
//         `<span style="display: inline-block; width: 20px; height: 10px; background-color: #4CAF50; margin-right: 5px;"></span>` +
//         `<span style="font-weight:bold; font-size: 15px">OK </span><span style="font-size: 15px">${ok}건 / ${okPercent}%</span><br>` +
//         `<span style="display: inline-block; width: 20px; height: 10px; background-color: #F44336; margin-right: 5px;"></span>` +
//         `<span style="font-weight:bold; font-size: 15px">NG </span><span style="font-size: 15px">${ng}건 / ${ngPercent}%</span>`;
// }


// 제품 달성률 바 차트의 막대 색상 결정
// function getBarColor(rate) {
//     if (rate < 40) return '#F44336';
//     if (rate < 70) return '#F39C12';
//     if (rate < 100) return '#F1C40F';
//     return '#4CAF50';
// }

// ================== 메인 실행 ==================
// document.addEventListener('DOMContentLoaded', function () {
//
//     // ▶ 1. 공정별 도넛 차트
//     drawChart(document.getElementById('myChart1'), ok1, ng1, 'chartText1');
//     drawChart(document.getElementById('myChart2'), ok2, ng2, 'chartText2');
//     drawChart(document.getElementById('myChart3'), ok3, ng3, 'chartText3');
//
//     // ▶ 1-1. 평균 NG율에 따른 경고 메시지 표시
//     const ngRates = [
//         (ng1 / (ok1 + ng1)) * 100,
//         (ng2 / (ok2 + ng2)) * 100,
//         (ng3 / (ok3 + ng3)) * 100
//     ];
//     const avgNg = ngRates.reduce((a, b) => a + b, 0) / ngRates.length;
//     const warningDiv = document.getElementById('ngWarningMessage');
//
//     let message = '', style = '', icon = '';
//     if (avgNg < 5) {
//         icon = '✅';
//         message = `${icon} 전체 공정 NG율 ${avgNg.toFixed(1)}% 미만 (양호)`;
//         style = 'background-color: #e0f5e9; color: #28a745;';
//     } else if (avgNg < 10) {
//         icon = '🔔';
//         message = `${icon} NG율 ${avgNg.toFixed(1)}% (주의 필요)`;
//         style = 'background-color: #fff9e6; color: #ffc107;';
//         warningDiv.className = 'warning-box blinking'; // 깜박이는 효과
//     } else {
//         icon = '🚨';
//         message = `${icon} 비상! NG율 ${avgNg.toFixed(1)}% 초과`;
//         warningDiv.className = 'warning-box blinking'; // 깜박이는 효과
//     }
//     warningDiv.setAttribute('style', `${style} padding: 5px 10px; border-radius: 10px; font-weight: bold;`);
//     warningDiv.textContent = message;

    // // ▶ 2. 제품 달성률 바 차트
    // const now = new Date();
    // document.getElementById('timeRange').textContent = `${now.getHours()}시 ~ ${now.getHours()}시 59분`;
    //
    // new Chart(document.getElementById('productAchievementChart').getContext('2d'), {
    //     type: 'bar',
    //     data: {
    //         labels: ['GyulRide', 'InteliBike', 'PedalAt4'],
    //         datasets: [{
    //             label: '현재 달성률 (%)',
    //             data: actualRates,
    //             backgroundColor: actualRates.map(getBarColor)
    //         }]
    //     },
    //     options: {
    //         indexAxis: 'y',
    //         responsive: true,
    //         layout: { padding: { right: 120 } },
    //         scales: {
    //             x: {
    //                 beginAtZero: true,
    //                 max: 100,
    //                 ticks: {
    //                     stepSize: 20,
    //                     font: { size: 14 },
    //                     callback: (val) => `${val}%`
    //                 }
    //             },
    //             y: {
    //                 ticks: { font: { size: 16 } }
    //             }
    //         },
    //         plugins: {
    //             legend: { display: false },
    //             tooltip: {
    //                 callbacks: {
    //                     label: (ctx) => {
    //                         const i = ctx.dataIndex;
    //                         return `${actualCounts[i]}/${targets[i]}대 (${actualRates[i].toFixed(1)}%)`;
    //                     }
    //                 }
    //             }
    //         }
    //     },
    //     plugins: [{
    //         id: 'barLabelPlugin',
    //         afterDatasetsDraw(chart) {
    //             const { ctx, scales: { x, y } } = chart;
    //             chart.data.datasets[0].data.forEach((percent, i) => {
    //                 const label = `${actualCounts[i]}/${targets[i]}대`;
    //                 ctx.save();
    //                 ctx.font = 'bold 16px sans-serif';
    //                 ctx.fillStyle = percent < 60 ? '#F44336' : '#333';
    //                 ctx.textAlign = 'left';
    //                 ctx.fillText(label, x.getPixelForValue(100) + 10, y.getPixelForValue(i));
    //                 ctx.restore();
    //             });
    //         }
    //     }]
    // });

    // ▶ 3. 이상 감지 도넛 차트
    datasets.forEach(data => {
        const canvas = document.getElementById(data.id);
        const ctx = canvas.getContext('2d');
        canvas.width = canvas.parentNode.offsetWidth;

        const bgColor = data.value <= 60 ? 'green' : (data.value <= 79 ? '#FFD700' : 'red');

        new Chart(ctx, {
            type: 'doughnut',
            data: {
                datasets: [{
                    data: [data.value, 100 - data.value],
                    backgroundColor: [bgColor, '#878787'],
                    borderWidth: 0
                }]
            },
            options: {
                maintainAspectRatio: false,
                responsive: true,
                circumference: 180,
                rotation: 270,
                cutout: '50%',
                plugins: {
                    legend: { display: false },
                    tooltip: { enabled: false }
                }
            },
            plugins: [{
                afterDraw: chart => { // 도넛 중앙에 수치 표시
                    const { ctx, chartArea: { width, height } } = chart;
                    const centerX = width / 2;
                    const centerY = height / 1.3;
                    ctx.save();
                    ctx.beginPath();
                    ctx.arc(centerX, centerY, 50, 0, 2 * Math.PI);
                    ctx.fillStyle = '#ffffff';
                    ctx.fill();
                    ctx.strokeStyle = '#F37221';
                    ctx.stroke();
                    ctx.font = '15px sans-serif';
                    ctx.fillStyle = 'black';
                    ctx.textAlign = 'center';
                    ctx.fillText(`${data.value}%`, centerX, centerY);
                    ctx.restore();
                }
            }]
        });

        // 상태 표시
        const statusDiv = document.getElementById(data.statusId);
        const warningDiv = document.getElementById(data.warningId);
        const isWarning = data.value >= 80;
        statusDiv.querySelector('.defect-btn').disabled = !isWarning;
        statusDiv.querySelector('.defect-btn').onclick = isWarning ? () => {
            window.location.href = '/indicators/defect-predict';
        } : null;
        statusDiv.querySelector('.defect-label').textContent = `${data.label} (${data.value}%)`;

        if (isWarning) {
            warningDiv.className = 'warning-box blinking warning-placeholder';
            warningDiv.innerHTML = `⚠ 주의! ${data.label} 초과 경고`;
        } else {
            warningDiv.className = 'warning-placeholder';
            warningDiv.innerHTML = '';
        }
    });
