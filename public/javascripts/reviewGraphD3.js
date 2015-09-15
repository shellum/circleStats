var data = [];
var graphHeight = 120;
var graphMargin = 35;
var graphWidth = 400;
var overallGraphsWidth = 500;
var overallGraphsHeight = 0;
var peerScoresHeight = 20;
var peerScoresMargin = 10;
var peerScoresTop = 35;
var roundedCornersBigRadius = 15;
var roundedCornersSmallRadius = 3;
var axisOffset = 25;
var peerScoresMaxWidth = graphWidth - (graphMargin * 2);
var maxRange = 10;
var markerDimension = 6;
var infitesimalDelta = 5;
var titleXOffset = 35;
var titleYOffset = 25;
var labelSize = 12;
var labelOffset = 8;
var selfColor = '#F5EE9E';
var managerColor = '#246EB9';
var fontFamily = 'Helvetica';
var graphBackgroundColor = '#fff';
var averagePeerColor = 'white';
var peerGradientStartEndColor = '#65bd2a';
var overallGraphBorderDetails = '';//1px black solid';
var xscale = d3.scale.linear().domain([0, 10]).range([0, graphWidth - (graphMargin * 2)]);
var xaxis = d3.svg.axis().scale(xscale).orient('bottom');

function start(data) {
  overallGraphsHeight = (graphHeight + graphMargin * 1.5) * data.length + graphMargin;

  d3.select('svg')
  .style('height', overallGraphsHeight)
  .style('width', overallGraphsWidth)
  .style('border', overallGraphBorderDetails)
  .style('margin-top', 50 + graphMargin);

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
  .attr('rx',roundedCornersBigRadius)
  .attr('ry',roundedCornersBigRadius)
  .style('fill',graphBackgroundColor)
  .style('margin',graphMargin);

  // Attribute graphs
  var graphs = d3.selectAll('g');

  // Add graph name
  graphs
  .append('text')
  .attr('x', titleXOffset)
  .attr('y', titleYOffset)
  .attr('font-family', fontFamily)
      .style('text-decoration','underline')
  .text(function(d, i) {
    return d.title;
  });

  // Add x-axis
  d3.select('svg')
  .selectAll('g')
  .append('g')
  .attr('transform', 'translate(' + graphMargin + ',' + (graphHeight - axisOffset) + ')')
  .attr('font-family', fontFamily)
  .attr('font-size', labelSize)
  .call(xaxis);

  // Peer group
  var peerGroup = graphs.append('g');
  var r = peerGroup
  .append('rect')
  .style('padding','10px')
      .style('fill', peerGradientStartEndColor)
      .attr('rx',roundedCornersSmallRadius)
      .attr('ry',roundedCornersSmallRadius)
      .attr('height', peerScoresHeight)
.attr('width', '10');
  r.transition().duration(1000)
      .attr('width', function(d, i) {
    var x = 0;
    if (d.peerScores != undefined) x = d3.max(d.peerScores) - d3.min(d.peerScores);
    if (x == 0) return infitesimalDelta;
    else return x * peerScoresMaxWidth / maxRange;
  } );

  var peerRect = peerGroup
  .append('rect')
      .attr('rx',roundedCornersSmallRadius)
      .attr('ry',roundedCornersSmallRadius)
  .attr('height', peerScoresHeight)
  .style('fill', averagePeerColor)
      .style('stroke','#333')
      .style('stroke-opacity','0.3')
      .attr('opacity','0.8')
      .transition().duration(1000)
  .attr('transform', function(d, i) {
    var x = 0;
    if (d.peerScores != undefined) x = d3.mean(d.peerScores) - d3.min(d.peerScores);
    if (x == 0) return 'translate(0, 0)';
    else return 'translate(' + (x * peerScoresMaxWidth / maxRange) + ', 0)';
  })
      .attr('width', infitesimalDelta);


  function getPeerText(d,i) {
    if (d.peerScores == undefined)
      return "No Peer Scores";
    return "Peer Score Range";
  }

  peerGroup
  .attr('transform', function(d, i) {
    var peerScoresMin = 0;
    if (d.peerScores != undefined)
      peerScoresMin = d3.min(d.peerScores);
    var x = ((peerScoresMin * peerScoresMaxWidth / maxRange) + graphMargin);
    var y = peerScoresTop;
    return 'translate(' + x + ',' + y + ')'
  })
  .append('text')
  .text(getPeerText)
  .attr('font-size', labelSize)
  .attr('font-family', fontFamily)
  .attr('transform', 'translate(' + labelOffset + ', ' + (peerScoresHeight - peerScoresMargin + labelOffset/2) + ')');

  var selfTranslateFunctionXY = function(d, i) {
    var selfScore = 0;
    if (d.selfScore != undefined)
      selfScore = d.selfScore;
    var x = ((selfScore * peerScoresMaxWidth / maxRange) + (graphMargin));
    var y = ((graphHeight - peerScoresHeight) / 2) + (markerDimension * 2);
    return 'translate(' + x + ','+y+')';
  };

  var selfTranslateFunctionY = function(d, i) {
    var selfScore = 0;
    if (d.selfScore != undefined)
      selfScore = d.selfScore;
    var x = ((selfScore * peerScoresMaxWidth / maxRange) + (graphMargin));
    var y = ((graphHeight - peerScoresHeight) / 2) + (markerDimension * 2);
    return 'translate(0,' + y + ')';
  };


  var selfGroup = graphs.append('g')
      .attr('transform', selfTranslateFunctionY);
      selfGroup.transition().duration(1000)
  .attr('transform', selfTranslateFunctionXY);
  selfGroup
  .append('circle')


      .attr('r', markerDimension)
  .attr('cx', markerDimension/4)
  .attr('cy', markerDimension/2)
  .style('fill', selfColor)
  .style('stroke','#333');

  function getSelfText(d,i) {
    if (d.selfScore == undefined)
      return "No Self Score";
    return "Self";
  }

  selfGroup.append('text')
  .text(getSelfText)
  .attr('font-size', labelSize)
  .attr('font-family', fontFamily)
  .attr('transform', 'translate(' + labelOffset + ', ' + labelOffset + ')');

  var managerTranslateFunctionY = function(d, i) {
    var mgrScore = 0;
    if (d.managerScore != undefined)
      mgrScore = d.managerScore;
    var x = ((mgrScore * peerScoresMaxWidth / maxRange) + (graphMargin));
    var y = ((graphHeight - peerScoresHeight) / 2) + (markerDimension * 4) + 5;
    return 'translate(0,' + y + ')'
  };
  var managerTranslateFunctionXY = function(d, i) {
    var mgrScore = 0;
    if (d.managerScore != undefined)
      mgrScore = d.managerScore;
    var x = ((mgrScore * peerScoresMaxWidth / maxRange) + (graphMargin));
    var y = ((graphHeight - peerScoresHeight) / 2) + (markerDimension * 4) + 5;
    return 'translate(' + x + ',' + y + ')'
  };

  var managerGroup = graphs.append('g')
      managerGroup.attr('transform', managerTranslateFunctionY);
  managerGroup.transition().duration(1000)
      .attr('transform', managerTranslateFunctionXY);


  managerGroup
  .append('circle')
  .attr('r', markerDimension)
  .attr('cx', markerDimension/4)
  .attr('cy', markerDimension/2)
      .style('stroke','#333')
  .style('fill', managerColor);

  function getManagerText(d,i) {
    if (d.managerScore == undefined)
      return "No Mgr Score";
    return "Mgr";
  }

  managerGroup.append('text')
  .text(getManagerText)
  .attr('font-size', labelSize)
  .attr('font-family', fontFamily)
  .attr('transform', 'translate(' + labelOffset + ', ' + labelOffset + ')');

}
