// generate chart to human health
var promise = d3.csv('data/human-health.csv');
promise.then(function(data) {

  var day = [];
  data.filter(function(row) {
    day.push(row.DAY);
  });

  var susceptible = [];
  data.filter(function(row) {
    susceptible.push(row.SUSCEPTIBLE);
  });

  var exposed = [];
  data.filter(function(row) {
    exposed.push(row.EXPOSED);
  });

  var mildInfection = [];
  data.filter(function(row) {
    mildInfection.push(row.MILD_INFECTION);
  });

  var severeInfection = [];
  data.filter(function(row) {
    severeInfection.push(row.SEVERE_INFECTION);
  });

  var toxicInfection = [];
  data.filter(function(row) {
    toxicInfection.push(row.TOXIC_INFECTION);
  });

  var recovered = [];
  data.filter(function(row) {
    recovered.push(row.RECOVERED);
  });

  var ctx = document.getElementById('human-health-chart');
  var myChart = new Chart(ctx, {
    type: 'line',
    data: {
      labels: day,
      datasets: [{
        label: 'Exposto',
        borderColor: window.chartColors.yellow,
        backgroundColor: window.chartColors.yellow,
        data: exposed,
        fill: false,
      }, {
        label: 'Infecção leve',
        borderColor: window.chartColors.pink,
        backgroundColor: window.chartColors.pink,
        data: mildInfection,
        fill: false,
      }, {
        label: 'Recuperado',
        borderColor: window.chartColors.blue,
        backgroundColor: window.chartColors.blue,
        data: recovered,
        fill: false,
      }]
    },
    options: {
      responsive: true,
      title: {
        display: true,
        text: 'Evolução da infecção no período de um mês sobre o agente humano'
      },
      scales: {
        xAxes: [{
          display: true,
          scaleLabel: {
            display: true,
            labelString: 'Tempo (dias)'
          }
        }],
        yAxes: [{
          display: true,
          scaleLabel: {
            display: true,
            labelString: 'Número de casos'
          }
        }]
      }
    }
  });

}, function(error) {});

// generate chart to human health
var promise = d3.csv('data/mosquito-state.csv');
promise.then(function(data) {

  var day = [];
  data.filter(function(row) {
    day.push(row.DAY);
  });

  var amount = [];
  data.filter(function(row) {
    amount.push(row.AMOUNT);
  });

  var carryEggs = [];
  data.filter(function(row) {
    carryEggs.push(row.CARRYING_EGGS);
  });

  var amountOfDead = [];
  data.filter(function(row) {
    amountOfDead.push(row.AMOUNT_OF_DEAD);
  });

  var ctx = document.getElementById('mosquito-state-chart');
  var myChart = new Chart(ctx, {
    type: 'line',
    data: {
      labels: day,
      datasets: [{
        label: 'Quantidade',
        borderColor: window.chartColors.yellow,
        backgroundColor: window.chartColors.yellow,
        data: amount,
        fill: false,
      }, {
        label: 'Carregando ovos',
        borderColor: window.chartColors.pink,
        backgroundColor: window.chartColors.pink,
        data: carryEggs,
        fill: false,
      }, {
        label: 'Mortos',
        borderColor: window.chartColors.blue,
        backgroundColor: window.chartColors.blue,
        data: amountOfDead,
        fill: false,
      }]
    },
    options: {
      responsive: true,
      title: {
        display: true,
        text: 'Estado dos vetores sobre o ambiente'
      },
      scales: {
        xAxes: [{
          display: true,
          scaleLabel: {
            display: true,
            labelString: 'Tempo (dias)'
          }
        }],
        yAxes: [{
          display: true,
          scaleLabel: {
            display: true,
            labelString: 'Quantidade'
          }
        }]
      }
    }
  });

}, function(error) {});
