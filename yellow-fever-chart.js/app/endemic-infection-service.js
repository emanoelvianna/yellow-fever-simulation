var day = [];
var susceptible = [];
var exposed = [];
var mildInfection = [];
var severeInfection = [];
var toxicInfection = [];
var recovered = [];

// read csv files
Promise.all([
  d3.csv('data/scenario-endemic-infection/1/human-health.csv'),
  d3.csv('data/scenario-endemic-infection/2/human-health.csv'),
  d3.csv('data/scenario-endemic-infection/3/human-health.csv'),
  d3.csv('data/scenario-endemic-infection/4/human-health.csv'),
  d3.csv('data/scenario-endemic-infection/5/human-health.csv'),
  d3.csv('data/scenario-endemic-infection/6/human-health.csv'),
  d3.csv('data/scenario-endemic-infection/7/human-health.csv'),
  d3.csv('data/scenario-endemic-infection/8/human-health.csv'),
  d3.csv('data/scenario-endemic-infection/9/human-health.csv'),
  d3.csv('data/scenario-endemic-infection/10/human-health.csv')
]).then(function(files) {
  files.filter(function(file) {
    file.filter(function(line, i) {
      if (susceptible.length <= 29) { // first prepare the list
        var values = [];
        values.push(Number(line.SUSCEPTIBLE));
        susceptible.push(values);
      } else {
        susceptible[i].push(Number(line.SUSCEPTIBLE));
      };

      if (exposed.length <= 29) { // first prepare the list
        var values = [];
        values.push(Number(line.EXPOSED));
        exposed.push(values);
      } else {
        exposed[i].push(Number(line.EXPOSED));
      };

      if (mildInfection.length <= 29) { // first prepare the list
        var values = [];
        values.push(Number(line.MILD_INFECTION));
        mildInfection.push(values);
      } else {
        mildInfection[i].push(Number(line.MILD_INFECTION));
      };

      if (severeInfection.length <= 29) { // first prepare the list
        var values = [];
        values.push(Number(line.SEVERE_INFECTION));
        severeInfection.push(values);
      } else {
        severeInfection[i].push(Number(line.SEVERE_INFECTION));
      };

      if (severeInfection.length <= 29) { // first prepare the list
        var values = [];
        values.push(Number(line.TOXIC_INFECTION));
        severeInfection.push(values);
      } else {
        severeInfection[i].push(Number(line.TOXIC_INFECTION));
      };

      if (recovered.length <= 29) { // first prepare the list
        var values = [];
        values.push(Number(line.RECOVERED));
        recovered.push(values);
      } else {
        recovered[i].push(Number(line.RECOVERED));
      };
    });
  });

  var i;
  for (i = 1; i <= 30; i++) {
    day.push(i);
  }

  // generate chart to standard deviation human health
  var ctx = document.getElementById('human-health-chart');
  var myChart = new Chart(ctx, {
    type: 'line',
    data: {
      labels: day,
      datasets: [{
        label: 'Exposto',
        borderColor: window.chartColors.yellow,
        backgroundColor: window.chartColors.yellow,
        data: _standardDeviationToExposed(),
        fill: false,
      }, {
        label: 'Média',
        borderColor: window.chartColors.green,
        backgroundColor: window.chartColors.green,
        data: _averageExposed(),
        fill: false,
      }, {
        label: 'Infecção leve',
        borderColor: window.chartColors.pink,
        backgroundColor: window.chartColors.pink,
        data: _standardDeviationToMildInfection(),
        fill: false,
      }, {
        label: 'Infecção severa',
        borderColor: window.chartColors.orange,
        backgroundColor: window.chartColors.orange,
        data: _standardDeviationToSevereInfection(),
        fill: false,
      }, {
        label: 'Recuperado',
        borderColor: window.chartColors.blue,
        backgroundColor: window.chartColors.blue,
        data: _standardDeviationToRecovered(),
        fill: false,
      }]
    },
    options: {
      responsive: true,
      title: {
        display: true,
        text: 'Desvio padrão sobre a evolução da infecção no período de um mês sobre o agente humano'
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
}).catch(function(error) {
  if (error) throw error;
});


function _averageExposed() {
  var n = exposed.length;
  var average = [];
  var results = [];
  exposed.filter(function(line, index) {
    var sum = 0;
    line.filter(function(value) {
      sum = sum + value;
    });
    average.push(sum / n);
  });

  console.log("Média:");
  console.log(average);
  return average;
}

// calculate standard deviation

function _standardDeviationToSusceptible() {
  var n = susceptible.length;
  var average = [];
  var results = [];
  susceptible.filter(function(line, index) {
    var sum = 0;
    var truncated = 0;

    line.filter(function(value) {
      sum = sum + value;
    });
    var truncated = Math.floor((sum / n) * 100) / 100;
    average.push(truncated);

    sum = 0;
    line.filter(function(value) {
      var result = value - average[index];
      sum = sum + Math.pow(value - average[index], 2);
    });

    truncated = Math.floor((sum / (n - 1)) * 100) / 100;
    results.push(truncated);
  });

  return results;
}

function _standardDeviationToExposed() {
  var n = exposed.length;
  var average = [];
  var results = [];
  exposed.filter(function(line, index) {
    var sum = 0;
    var truncated = 0;

    line.filter(function(value) {
      sum = sum + value;
    });
    average.push(sum / n);

    sum = 0;
    line.filter(function(value) {
      var result = value - average[index];
      sum = sum + Math.pow(value - average[index], 2);
    });
    results.push(sum / (n - 1));
  });

  console.log("desvio padrão:");
  console.log(results);
  return results;
}

function _standardDeviationToMildInfection() {
  var n = mildInfection.length;
  var average = [];
  var results = [];
  mildInfection.filter(function(line, index) {
    var sum = 0;
    var truncated = 0;

    line.filter(function(value) {
      sum = sum + value;
    });
    var truncated = Math.floor((sum / n) * 100) / 100;
    average.push(truncated);

    sum = 0;
    line.filter(function(value) {
      var result = value - average[index];
      sum = sum + Math.pow(value - average[index], 2);
    });

    truncated = Math.floor((sum / (n - 1)) * 100) / 100;
    results.push(truncated);
  });

  return results;
}

function _standardDeviationToSevereInfection() {
  var n = severeInfection.length;
  var average = [];
  var results = [];
  severeInfection.filter(function(line, index) {
    var sum = 0;
    var truncated = 0;

    line.filter(function(value) {
      sum = sum + value;
    });
    var truncated = Math.floor((sum / n) * 100) / 100;
    average.push(truncated);

    sum = 0;
    line.filter(function(value) {
      var result = value - average[index];
      sum = sum + Math.pow(value - average[index], 2);
    });

    truncated = Math.floor((sum / (n - 1)) * 100) / 100;
    results.push(truncated);
  });

  return results;
}

function _standardDeviationToToxicInfection() {
  var n = toxicInfection.length;
  var average = [];
  var results = [];
  toxicInfection.filter(function(line, index) {
    var sum = 0;
    var truncated = 0;

    line.filter(function(value) {
      sum = sum + value;
    });
    var truncated = Math.floor((sum / n) * 100) / 100;
    average.push(truncated);

    sum = 0;
    line.filter(function(value) {
      var result = value - average[index];
      sum = sum + Math.pow(value - average[index], 2);
    });

    truncated = Math.floor((sum / (n - 1)) * 100) / 100;
    results.push(truncated);
  });

  return results;
}

function _standardDeviationToRecovered() {
  var n = recovered.length;
  var average = [];
  var results = [];
  recovered.filter(function(line, index) {
    var sum = 0;
    var truncated = 0;

    line.filter(function(value) {
      sum = sum + value;
    });
    average.push((sum / n));

    sum = 0;
    line.filter(function(value) {
      sum = sum + Math.pow(value - average[index], 2);
    });

    truncated = Math.floor((sum / (n - 1)) * 100) / 100;
    results.push(truncated);
  });

  return results;
}
