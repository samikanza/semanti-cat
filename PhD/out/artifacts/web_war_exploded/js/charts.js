/**
 * Created by samikanza on 17/08/2017.
 */
var margin = {
    top: 15,
    right: 25,
    bottom: 15,
    left:130
};

var width = 700 - margin.left - margin.right;
var height = 300 - margin.top - margin.bottom;

var color = d3.scale.ordinal()
    .range(["#1A8B9D",
        "#21294C",
        "#4E3188",
        "#0092CA",
        "#A5E9E1",
        "#286b55",
        "#AEA1EA"]);

$(document).ready(function(){

    createChords();
    //createDonuts();
    //createBarCharts();
    createHorizontalBarCharts();


});

function createChords(){
    $('.chordMatrix').each(function(){
        var fileName = "matricies/" + $(this).attr('id') + ".csv";
        var id = "#" + $(this).attr('id');
        createChord(fileName,id);
    });

    if($('#rxno-matrix').length == 1){
        createChord("matricies/rxno-matrix.csv", "#rxno-matrix");
    }if($('#chmo-matrix').length == 1){
        createChord("matricies/chmo-matrix.csv", "#chmo-matrix");
    }if($('#mop-matrix').length == 1){
        createChord("matricies/mop-matrix.csv", "#mop-matrix");
    }if($('#physics-matrix').length == 1){
        createChord("matricies/physics-matrix.csv", "#physics-matrix");
    }if($('#cl-matrix').length == 1){
        createChord("matricies/cl-matrix.csv", "#cl-matrix");
    }if($('#po-matrix').length == 1){
        createChord("matricies/po-matrix.csv", "#po-matrix");
    }
}

// function createDonuts(){
//
//     $('.donut').each(function(){
//         var svg = d3.select("#" + $(this).attr('id')).append("svg")
//             .attr("width", width)
//             .attr("height", height)
//             .append("g")
//             .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");
//         createDonut($(this).data('counts'),svg);
//     });
//
// }
//
// function createDonut(csv,svg){
//     var data = d3.csv.parse(csv);
//
//     var tip = d3.tip()
//         .attr('class', 'd3-tip')
//         .html(function(d) { return d.data.term + " : " + d.data.count; })
//         .direction('s')
//
//     svg.call(tip);
//
//     var g = svg.selectAll(".arc")
//         .data(pie(data))
//         .enter().append("g")
//         .attr("class", "arc")
//         .attr("d", arc)
//         .on('mouseover',tip.show)
//         .on('mouseout', tip.hide);
//
//     g.append("path")
//         .attr("d", arc)
//         .style("fill", function(d) { return color(d.data.term); });
//
//     g.append("text")
//         .attr("transform", function(d) { return "translate(" + arc.centroid(d) + ")"; })
//         .attr("dy", ".35em")
//         .text(function(d) { return d.count; });
// }

// function createBarCharts(){
//     var margin = {top: 20, right: 20, bottom: 70, left: 40},
//         width = 600 - margin.left - margin.right,
//         height = 300 - margin.top - margin.bottom;
//
//     $('.bar-chart').each(function(){
//         var svg = d3.select("#" + $(this).attr('id')).append("svg")
//             .attr("width", width + margin.left + margin.right)
//             .attr("height", height + margin.top + margin.bottom)
//             .append("g")
//             .attr("transform",
//                 "translate(" + margin.left + "," + margin.top + ")");
//         createBarChart($(this).data('counts'),svg);
//     });
//
// }

// function createBarChart(csv,svg){
//     var data = d3.csv.parse(csv);
//
//     data.forEach(function(d) {
//         d.term = (d.term).toString();
//         d.count = +d.count;
//     });
//
//     var x = d3.scale.ordinal().rangeRoundBands([0, width], .05);
//     var y = d3.scale.linear().range([height, 0]);
//
//     var xAxis = d3.svg.axis()
//         .scale(x)
//         .orient("bottom")
//         .tickFormat(d3.String);
//
//     var yAxis = d3.svg.axis()
//         .scale(y)
//         .orient("left")
//         .ticks(10);
//
//     x.domain(data.map(function(d) { return d.term; }));
//     y.domain([0, d3.max(data, function(d) { return d.count; })]);
//
//     svg.append("g")
//         .attr("class", "x axis")
//         .attr("transform", "translate(0," + height + ")")
//         .call(xAxis)
//         .selectAll("text")
//         .style("text-anchor", "end")
//         .attr("dx", "-.8em")
//         .attr("dy", "-.55em")
//         .attr("transform", "rotate(-90)" );
//
//     svg.append("g")
//         .attr("class", "y axis")
//         .call(yAxis)
//         .append("text")
//         .attr("transform", "rotate(-90)")
//         .attr("y", 6)
//         .attr("dy", ".71em")
//         .style("text-anchor", "end")
//         .text("Value ($)");
//
//     svg.selectAll("bar")
//         .data(data)
//         .enter().append("rect")
//         .style("fill", "#1A8B9D")
//         .attr("x", function(d) { return x(d.count); })
//         .attr("width", x.rangeBand())
//         .attr("y", function(d) { return y(d.term); })
//         .attr("height", function(d) { return height - y(d.count); });
// }

function createHorizontalBarCharts(){
    $('.bar-chart').each(function(){
        var svg = d3.select("#" + $(this).attr('id')).append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
        createHorizontalBarChart($(this).data('counts'),svg);
    });
}

function createHorizontalBarChart(csv,svg) {
    var data = d3.csv.parse(csv);

    data.forEach(function (d) {
        d.term = d.term;
        d.count = +d.count;
    });

    data = data.sort(function (a, b) {
        return d3.ascending(a.count, b.count);
    })

    // var x = d3.scale.linear()
    // .range([0, width], .7)
    //     .domain([0, d3.max(data, function (d) {
    //         return d.count;
    //     })]);

    var y = d3.scale.ordinal()
        .rangeRoundBands([height, 0], .7)
        .domain(data.map(function (d) {
            return d.term;
        }));

    //make y axis to show bar names
    var yAxis = d3.svg.axis()
        .scale(y)
        //no tick marks
        .tickSize(0)
        .orient("left");

    var gy = svg.append("g")
        .attr("class", "y axis")
        .call(yAxis)

    var bars = svg.selectAll(".bar")
        .data(data)
        .enter()
        .append("g")

    //append rects
    bars.append("rect")
        .attr("class", "bar")
        .attr("y", function (d) {
            return y(d.term);
        })
        .attr("height", y.rangeBand())
        .attr("x", 0)
        .attr("width", function (d) {
            return d.count * 40;
        });

    //add a value label to the right of each bar
    bars.append("text")
        .attr("class", "label")
        //y position of the label is halfway down the bar
        .attr("y", function (d) {
            return y(d.term) + y.rangeBand() / 2 + 4;
        })
        //x position is 3 pixels to the right of the bar
        .attr("x", function (d) {
            //return x(d.count) + 3;
            return d.count * 40 + 3;
        })
        .text(function (d) {
            return d.count;
        });
}