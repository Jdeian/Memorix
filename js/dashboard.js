document.addEventListener("DOMContentLoaded", function () {
    let barChartInstance; 
    let donutChartInstance;

    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

    function createBarChart(free, basic, premium) {
        const barCtx = document.getElementById('barChart').getContext('2d');

        if (barChartInstance) {
            barChartInstance.destroy();
        }

        const barData = {
            labels: months,
            datasets: [
                {
                    label: 'Basic Users',
                    data: basic,
                    backgroundColor: '#0E43FB',
                },
                {
                    label: 'Premium Users',
                    data: premium,
                    backgroundColor: '#00C2FF', 
                }
            ]
        };

        barChartInstance = new Chart(barCtx, {
            type: 'bar',
            data: barData,
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        display: false 
                    },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                return context.dataset.label + ': ' + context.raw + ' users';
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        stacked: true,
                        barPercentage: 0.4,
                        ticks: {
                            color: '#AEB9E1',
                        }
                    },
                    y: {
                        stacked: true,
                        beginAtZero: true,
                        ticks: {
                            color: '#AEB9E1',
                            padding: 10,
                            callback: function (value) {
                                return Number.isInteger(value) ? value : ''; 
                            }
                        },
                    },
                }
            }
        });
    }

    function createDonutChart(free, basic, premium) {
        const donutCtx = document.getElementById('donutChart').getContext('2d');

        if (donutChartInstance) {
            donutChartInstance.destroy();
        }

        const donutData = {
            labels: ['Free Users', 'Basic Users', 'Premium Users'],
            datasets: [{
                data: [free, basic, premium],
                backgroundColor: ['#02EBB1', '#0E43FB', '#00C2FF'],
                hoverOffset: 4
            }]
        };

        donutChartInstance = new Chart(donutCtx, {
            type: 'doughnut',
            data: donutData,
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        display: false 
                    },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                return context.label + ': ' + context.raw + ' users';
                            }
                        }
                    }
                }
            }
        });
    }

    const yearSelector = document.getElementById('year-selector');

    function fetchUserData(selectedYear) {
        fetch(`../includes/dashboard.inc.php?year=${selectedYear}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                const freeUsers = data.freeUsers;
                const basicUsers = data.basicUsers;
                const premiumUsers = data.premiumUsers;
              
                document.getElementById("free-user-count").textContent = freeUsers.reduce((a, b) => a + b, 0); 
                document.getElementById("basic-user-count").textContent = basicUsers.reduce((a, b) => a + b, 0); 
                document.getElementById("premium-user-count").textContent = premiumUsers.reduce((a, b) => a + b, 0); 

                createBarChart(freeUsers, basicUsers, premiumUsers);
                createDonutChart(freeUsers.reduce((a, b) => a + b, 0), basicUsers.reduce((a, b) => a + b, 0), premiumUsers.reduce((a, b) => a + b, 0));
            })
            .catch(error => console.error('Error fetching user data:', error));
    }

    yearSelector.addEventListener('change', function () {
        const selectedYear = this.value;
        fetchUserData(selectedYear);
    });

    fetchUserData(yearSelector.value);
});
