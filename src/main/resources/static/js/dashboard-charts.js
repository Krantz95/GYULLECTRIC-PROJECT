// 대시보드 추가할 차트들 모아놓고
// <th:block layout:fragment="script">
//     <script src="/js/dashboard-charts.js"></script>
// </th:block> 를 이용하여 집어넣기

// 1. 제품별 생산현황 (by. ProcessList.html)
function drawChart(ctx, ok, ng, textElementId) {
    const total = ok + ng;
    const okPercent = total > 0 ? ((ok / total) * 100).toFixed(0) : 0;
    const ngPercent = total > 0 ? ((ng / total) * 100).toFixed(0) : 0;

    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['OK', 'NG'],
            datasets: [{
                data: [ok, ng],
                backgroundColor: ['#4CAF50', '#F44336'],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            cutout: '40%',
            plugins: {
                legend: {display: false}
            }
        }
    });

    const textDiv = document.getElementById(textElementId);
    textDiv.innerHTML =
        `<span style="display: inline-block; width: 20px; height: 10px; background-color: #4CAF50; margin-right: 5px;"></span>` +
        `<span style="font-weight:bold; font-size: 15px">OK </span><span style="font-size: 15px">${ok}건 / ${okPercent}%</span><br>` +
        `<span style="display: inline-block; width: 20px; height: 10px; background-color: #F44336; margin-right: 5px;"></span>` +
        `<span style="font-weight:bold; font-size: 15px">NG </span><span style="font-size: 15px">${ng}건 / ${ngPercent}%</span>`;
}

document.addEventListener('DOMContentLoaded', function () {
    // 각 공정 차트 그리기
    drawChart(document.getElementById('myChart1'), 90, 10, 'chartText1');
    drawChart(document.getElementById('myChart2'), 90, 10, 'chartText2');
    drawChart(document.getElementById('myChart3'), 90, 10, 'chartText3');

    // NG율 평균 계산
    const ngRates = [
        (30 / (70 + 30)) * 100,  // 공정1
        (70 / (30 + 70)) * 100,  // 공정2
        (55 / (45 + 55)) * 100   // 공정3
    ];
    const avgNg = ngRates.reduce((sum, val) => sum + val, 0) / ngRates.length;

    // 경고 메시지 요소 가져오기
    const warningDiv = document.getElementById('ngWarningMessage');

    // 메시지 & 스타일 설정
    let message = '';
    let style = '';
    let icon = '';

    if (avgNg < 10) {
        icon = '✅';
        message = `${icon} 전체 공정 NG율 ${avgNg.toFixed(1)}% 미만 (양호)`;  // ← 반드시 메시지 채워줌
        style = 'background-color: #e0f5e9; color: #28a745; padding: 5px 10px; border-radius: 10px; font-weight: bold;';
        warningDiv.className = 'warning-placeholder';
        warningDiv.setAttribute('style', style);
        warningDiv.textContent = message;
    } else if (avgNg >= 10 && avgNg < 20) {
        icon = '🔔';
        message = `${icon} 전체 공정 NG율 ${avgNg.toFixed(1)}% (주의 필요)`;
        style = 'background-color: #fff9e6; color: #ffc107; padding: 5px 10px; border-radius: 10px; font-weight: bold;';
        warningDiv.className = 'warning-placeholder';
        warningDiv.setAttribute('style', style);
        warningDiv.textContent = message;
    } else if (avgNg >= 20 && avgNg < 30) {
        icon = '⚠';
        message = `${icon} 심각! NG율 ${avgNg.toFixed(1)}% (라인 점검 필요!)`;
        style = 'background-color: #ffe6f0; color: #ff4d4d; padding: 5px 10px; border-radius: 10px; font-weight: bold;';
        warningDiv.className = 'warning-placeholder';
        warningDiv.setAttribute('style', style);
        warningDiv.textContent = message;
    } else if (avgNg >= 30) {
        icon = '🚨';
        message = `${icon} 비상! 전체 공정 NG율 ${avgNg.toFixed(1)}% 초과 (공정 중단 필요!)`;
        warningDiv.className = 'warning-box blinking';
        warningDiv.textContent = message;
    } else {
        message = `전체 공정 NG율 ${avgNg.toFixed(1)}%`;
        warningDiv.className = 'warning-placeholder';
        warningDiv.textContent = message;
    }


    // 2. 제품별 생산 달성률(%) - 가로 막대 차트
    const productCtx = document.getElementById('productAchievementChart').getContext('2d');
    new Chart(productCtx, {
        type: 'bar',
        data: {
            labels: ['A타입', 'B타입', 'C타입'],
            datasets: [{
                label: '달성률 (%)',
                data: [60, 100, 25],
                backgroundColor: ['#FFD700', '#4CAF50', '#FF8C42']
            }]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            scales: {
                x: {
                    max: 100,
                    ticks: {
                        stepSize: 20
                    }
                },
            },
            plugins: {
                legend: {display: false},
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            return `${context.dataset.label}: ${context.parsed.x}%`;
                        }
                    }
                }
            }
        }
    });

    // 4. 기계 이상 감지 (from. defectLog.html)
    const datasets = [
        {id: 'castingChart', statusId: 'castingStatus', warningId: 'castingWarning', label: '압력/온도 감지', value: 80}, // 주조
        {id: 'weldingChart', statusId: 'weldingStatus', warningId: 'weldingWarning', label: '용접 출력 감지', value: 62}   // 용접
    ];

    datasets.forEach(data => {
        const canvas = document.getElementById(data.id);
        const ctx = canvas.getContext('2d');

        // canvas 크기 강제 지정
        canvas.height = 200;
        canvas.width = canvas.parentNode.offsetWidth;

        let bgColor;
        if (data.value <= 60) {
            bgColor = 'green';
        } else if (data.value <= 79) {
            bgColor = '#FFD700';
        } else {
            bgColor = 'red';
        }

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
                maintainAspectRatio: true,
                circumference: 180,
                rotation: 270,
                cutout: '50%',
                plugins: {
                    legend: {display: false},
                    tooltip: {
                        callbacks: {
                            label: function () {
                                return `${data.label}: ${data.value}% (기준 100%)`;
                            }
                        }
                    }
                }
            },
            plugins: [{
                afterDraw: chart => {
                    const {ctx, chartArea: {width, height}} = chart;
                    const centerX = width / 2;
                    const centerY = height / 1.5;
                    const smallRadius = 40;

                    ctx.save();
                    ctx.beginPath();
                    ctx.arc(centerX, centerY, smallRadius, 0, 2 * Math.PI);
                    ctx.fillStyle = '#ffffff';
                    ctx.fill();
                    ctx.strokeStyle = '#F37221';
                    ctx.lineWidth = 1;
                    ctx.stroke();
                    ctx.closePath();

                    ctx.font = '15px sans-serif';
                    ctx.fillStyle = 'black';
                    ctx.textAlign = 'center';
                    ctx.textBaseline = 'middle';
                    ctx.fillText(`${data.value}%`, centerX, centerY);

                    ctx.restore();
                }
            }]
        });

        const statusDiv = document.getElementById(data.statusId);
        const statusLabel = statusDiv.querySelector('.status-label');
        const statusButton = statusDiv.querySelector('.status-btn');

        // 상태 텍스트
        statusLabel.textContent = `${data.label} (${data.value}%)`;

        // 버튼 활성/비활성
        if (data.value >= 80) {
            statusButton.disabled = false;
        } else {
            statusButton.disabled = true;
        }

        // 경고 메시지 처리
        const warningDiv = document.getElementById(data.warningId);
        if (data.value >= 80) {
            warningDiv.className = 'warning-box blinking warning-placeholder';
            warningDiv.innerHTML = `⚠ 주의! ${data.label} 초과 경고`;
        } else {
            warningDiv.className = 'warning-placeholder';
            warningDiv.innerHTML = '';
        }
    });
});
