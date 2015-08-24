var graphHeight = 100;
var graphMargin = 10;
var graphWidth = 400;
var overallGraphsWidth = 500;
var overallGraphsHeight = 500;
var peerScoresHeight = 50;
var peerScoresMargin = 10;
var axisOffset = 25;
var peerScoresMaxWidth = graphWidth-(2*peerScoresMargin);
var maxRange = 10;
var markerDimension = 10;
var infitesimalDelta = 5;
var titleXOffset = 10;
var titleYOffset = 15;
var graphBackgroundColor = '#eeeeee';
var averagePeerColor = 'white';
var peerGradientStartEndColor = 'lightblue';
var overallGraphBorderDetails = '1px black solid';
var xscale = d3.scale.linear().domain([0, 10]).range([0, graphWidth - (graphMargin * 2)]);
var xaxis = d3.svg.axis().scale(xscale).orient('bottom');
var data = [
  {id: 1, title: 'Awesomeness', peerScores: [0,1,3,3,3,3,3,6,7,10], selfScore: 9, managerScore: 10},
  {id: 2, title: 'Creative', peerScores: [3,3], selfScore: 7, managerScore: 6},
  {id: 3, title: 'Helpful', peerScores: [0,0,0,0,5], selfScore: 5, managerScore: 8},
  {id: 4, title: 'Challenging', peerScores: [4,5,5], selfScore: 2, managerScore: 3}
];
$(function() {
  start();
});
function start() {
  d3.select('svg')
    .style('height', overallGraphsHeight)
    .style('width', overallGraphsWidth)
    .style('border', overallGraphBorderDetails);

  var group = d3.select('svg')
    .selectAll('rect')
    .call(xaxis)
    .data(data)
    .enter()
    .append('g')
    .attr('transform', function(d, i) {return 'translate(' + (graphMargin) + ', ' + (((i + 1) * graphMargin) + i * graphHeight) + ')';})
    .append('rect')
    .attr('height', graphHeight)
    .attr('width', graphWidth)
    .style('fill',graphBackgroundColor)
    .style('margin',graphMargin);

  var gradients = d3.select('defs')
    .selectAll('linearGradient')
    .data(data)
    .enter()
    .append('linearGradient')
    .attr('id', function(d, i){return 'gradient' + d.id;})
    .attr('x1', '0')
    .attr('x2', '1')
    .attr('y1', '0')
    .attr('y2', '0');

  gradients.append('stop')
    .attr('offset', '0%')
    .attr('stop-color', peerGradientStartEndColor);
  gradients.append('stop')
    .attr('offset', function(d, i) {
      var a = (d3.mean(d.peerScores) - d3.min(d.peerScores));
      var b = (d3.max(d.peerScores) - d3.min(d.peerScores));
      if (a==0 || b==0)
        return '50%';
      else
        return '' + Math.floor(a / b * 100) + '%';
    })
    .attr('stop-color', averagePeerColor);
  gradients.append('stop')
    .attr('offset', '100%')
    .attr('stop-color', peerGradientStartEndColor);

  var graphs = d3.selectAll('g');

  graphs
    .append('text')
    .attr('x', titleXOffset)
    .attr('y', titleYOffset)
    .text(function(d, i) {
      return d.title;
    });

  graphs
    .append('rect')
    .attr('height', peerScoresHeight)
    .attr('width', function(d, i) {
      var x = (d3.max(d.peerScores) - d3.min(d.peerScores));
      if (x == 0) return infitesimalDelta;
      else return x * peerScoresMaxWidth / maxRange;
    } )
    .attr('transform', function(d, i) {
      var x = ((d3.min(d.peerScores) * peerScoresMaxWidth / maxRange) + peerScoresMargin);
      var y = ((graphHeight - peerScoresHeight) / 2);
      return 'translate(' + x + ',' + y + ')'
    })
    .style('fill', function(d, i) { return 'url(#gradient' + d.id + ')';});

  graphs
    .append('rect')
    .attr('height', markerDimension)
    .attr('width', markerDimension)
    .attr('transform', function(d, i) {
      var x = ((d.selfScore * peerScoresMaxWidth / maxRange) + (markerDimension / 2));
      var y = ((graphHeight - peerScoresHeight) / 2);
      return 'translate(' + x + ',' + y + ')'
    });

  graphs
    .append('rect')
    .attr('height', markerDimension)
    .attr('width', markerDimension)
    .attr('transform', function(d, i) {
      var x = ((d.managerScore * peerScoresMaxWidth / maxRange) + (markerDimension / 2));
      var y = ((graphHeight - peerScoresHeight) / 2) + markerDimension;
      return 'translate(' + x + ',' + y + ')'
    })
    .style('fill', 'purple');

  d3.select('svg')
    .selectAll('g')
    .append('g')
    .attr('transform', 'translate(' + graphMargin + ',' + (graphHeight - axisOffset) + ')')
    .call(xaxis);
}
