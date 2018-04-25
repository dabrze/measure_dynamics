% setup
close all;
clear all;
format compact;
syms a b c d

% parameters
scale = 2;
gradient_color_resolution = 65;
exponent = 1;
palette = colormap(brewermap(gradient_color_resolution,'*RdBu'));
bin_num = 256;
proportions = [-1 0.9375, 0.800, 0.500, 0.200, 0.0625];

% auxiliary variables
n = a + b + c + d;
N = b + d ;
P = a + c;
P_ = a + b;
N_ = c + d;
sensitivity = a /(a+c);
specificity = d /(d+b);
precision = a/(a+b);
recall = sensitivity;
accuracy = (a+d)/(a+b+c+d);

% measures
measures = containers.Map;
measures('accuracy') = accuracy;
measures('G-mean') = sqrt(a/(a+c)*d/(d+b));
measures('balanced_accuracy') = (a/(a+c)+d/(d+b))/2;
measures('precision') = precision;
measures('recall') = recall;
measures('F1-score') = 2*(precision * recall) / (precision + recall);
measures('MCC') = (a*d-b*c) / sqrt(((a+b)*(b+d)*(a+c)*(c+d)));
measures('Kappa') = (accuracy - 1/n*(P*P_/n + N*N_/n)) / (1 - 1/n*(P*P_/n + N*N_/n));


bins = containers.Map;
bins('accuracy') = bin_num;
bins('G-mean') = bin_num;
bins('balanced_accuracy') = bin_num;
bins('precision') = bin_num;
bins('recall') = bin_num;
bins('F1-score') = bin_num;
bins('MCC') = bin_num;
bins('Kappa') = bin_num;

y_max = 0.025;
axes = containers.Map;
axes('accuracy') = [0 1 0 y_max];
axes('G-mean') = [0 1 0 y_max];
axes('balanced_accuracy') = [0 1 0 y_max];
axes('precision') = [0 1 0 y_max];
axes('recall') = [0 1 0 y_max];
axes('F1-score') = [0 1 0 y_max];
axes('MCC') = [-1 1 0 y_max];
axes('Kappa') = [-1 1 0 y_max];

%%%%%%%%%%%%
% skeleton
%%%%%%%%%%%%
visualize_skeleton();
disp('Press a key to see measure gradients...')
pause;

%%%%%%%%%%%%
% gradients
%%%%%%%%%%%%
k = keys(measures);
v = values(measures);
for i = 1:length(measures)
    g = figure(i);
    visualize_gradients(v{i}, scale, exponent, palette);
%     print(g, strcat('../../images/', k{i}, '_gradients.pdf'), '-dpdf','-r0');
    title(k{i});
end
tilefigs

%%%%%%%%%%%%
% histograms
%%%%%%%%%%%%
k = keys(measures);
v = values(measures);
b_v = values(bins);
a_v = values(axes);

for i = 1:length(measures)
    disp(strcat('Press a key to see histograms of ', {' '}, k{i}, '...'))
    pause;
    close all;

    for j = 1:length(proportions)
        h = figure(j);
        visualize_histograms(v{i}, b_v{i}, proportions(j));
        ax = gca;
        ax.YAxis.TickLabelFormat = '%,.3f';
        ax.YAxis.Exponent = 0;
        set(gca, 'YTick', []);
        axis(a_v{i});
        set(h,'Units','Inches');
        pos = get(h,'Position');
        set(h,'PaperPositionMode','Auto','PaperUnits','Inches','PaperSize',[pos(3), pos(4)])
%         print(h, strcat('../../images/', k{i}, '_p_', strrep(num2str(proportions(j)), '.', '_'), '.pdf'), '-dpdf','-r0');
        title(strcat(k{i}, {' p='}, num2str(proportions(j))));
    end
    tilefigs
end
