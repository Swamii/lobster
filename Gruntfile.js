'use strict';

module.exports = function(grunt) {
	grunt.initConfig({
		pkg: grunt.file.readJSON('package.json'),
        concat: {
            dist: {
                src: ['public/javascripts/**/*.js'],
                dest: 'public/js/vendor.js'
            }
        },
        coffee: {
			files: {
				src: ['public/app/**/*.coffee'],
				dest: 'public/js/app.js'
			}
		},
		watch: {
			files: ['public/app/**/*.coffee'],
			tasks: 'default'
		}
	});

    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-contrib-coffee');
	grunt.loadNpmTasks('grunt-contrib-concat');

    grunt.registerTask('default', ['coffee']);
};